(ns sharkybot2.core
  (:require
    [clojure.pprint :refer [pprint]]
    [sharkybot2.state :refer :all]
    [sharkybot2.flags :refer :all]
    [sharkybot2.users :refer :all]
    [sharkybot2.irc :refer :all]
    [sharkybot2.triggers :refer :all]
    sharkybot2.userinfo
    sharkybot2.spoilers
    sharkybot2.memory
    sharkybot2.amusements
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
  (load-state)
  (pprint @opts)
  (let [port (getopt :port)
        host (getopt :server)
        nick (first (getopt :nick))
        join (getopt :join)
        _ (println (str "Connecting to " host ":" port " as " nick))
        server (irc/connect host port nick :callbacks callbacks)]
    (println "Connected! Joining" join)
    (irc/join server join)
    (println "Done.")))
