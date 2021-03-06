(ns sharkbot.core
  (:require
    [sharkbot.state :refer :all]
    [sharkbot.flags :refer :all]
    [sharkbot.users :refer :all]
    [sharkbot.irc :refer :all]
    [sharkbot.triggers :refer :all]
    [irclj.core :as irc]
    )
  (:gen-class))

; We define this trivial function here so that we can hot-reload triggers and pick up
; the new definition of on-irc.
(defn dispatch [& args]
  (apply on-irc args))

(defn raw-log [server dir raw]
  (when (and (= :write dir)
             (not (.startsWith raw "PONG :")))
    (println ">>" raw)))

(defn die [& args]
  (println "Something went wrong, exiting")
  (prn args)
  (System/exit 1))

(def callbacks
  {:privmsg dispatch
   :ctcp-action dispatch
   :join (fn [server msg] (dispatch server (assoc msg :target (-> msg :params first))))
   :part dispatch
   :quit dispatch
   :366 (fn [server msg] (dispatch server (assoc msg :target (-> msg :params second))))
   :raw-log raw-log
   :on-shutdown die
   :on-exception die
   })

(defn -main
  [& args]
  (when-let [errors (parse-opts args)]
    (println (clojure.string/join "\n" errors))
    (println (:summary @opts))
    (System/exit 1))
  (println (:errors @opts))
  (when (getopt :help)
    (println (:summary @opts))
    (System/exit 0))
  (load-state)
  (reload-modules (map (partial str "sharkbot.modules.") (getopt :modules)))
  (let [port (getopt :port)
        host (getopt :server)
        nick (first (getopt :nick))
        _ (println (str "Connecting to " host ":" port " as " nick))
        server (irc/connect host port nick :pass (getopt :pass) :callbacks callbacks)]
    ; HACK HACK HACK
    ; enable keepalive on the socket, otherwise if it times out we never notice and the bot just hangs
    (-> @server :connection :socket (.setKeepAlive true))
    (irc/join server (getopt :join))))
