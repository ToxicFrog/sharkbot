(ns sharkbot.core
  (:require
    [clojure.pprint :refer [pprint]]
    [sharkbot.state :refer :all]
    [sharkbot.flags :refer :all]
    [sharkbot.users :refer :all]
    [sharkbot.irc :refer :all]
    [sharkbot.triggers :refer :all]
    sharkbot.userinfo
    sharkbot.spoilers
    sharkbot.memory
    sharkbot.amusements
    [irclj.core :as irc]
    )
  (:gen-class))

; We define this trivial function here so that we can hot-reload triggers and pick up
; the new definition of on-irc.
(defn dispatch [& args]
  (apply on-irc args))

(defn raw-log [server dir raw]
  (when (= :write dir)
    (println ">>" raw)))

(def callbacks
  {:privmsg dispatch
   :ctcp-action dispatch
   :join (fn [server msg] (dispatch server (assoc msg :target (-> msg :params first))))
   :part dispatch
   :quit dispatch
   :366 (fn [server msg] (dispatch server (assoc msg :target (-> msg :params second))))
   :raw-log raw-log
   })

(defn -main
  [& args]
  (parse-opts args)
  (when (getopt :help)
    (println (:summary @opts))
    (System/exit 0))
  (load-state)
  (let [port (getopt :port)
        host (getopt :server)
        nick (first (getopt :nick))
        join (getopt :join)
        _ (println (str "Connecting to " host ":" port " as " nick))
        server (irc/connect host port nick :callbacks callbacks)]
    (println "Connected! Joining" join)
    (irc/join server join)
    (println "Done.")))
