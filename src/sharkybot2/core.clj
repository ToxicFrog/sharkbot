(ns sharkybot2.core
  (:require
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
    [clojure.string :as string]
    )
  (:gen-class))

(def callbacks
  {:privmsg on-irc
   :ctcp-action on-irc
   :join (fn [server msg] (on-irc server (assoc msg :target (-> msg :params first))))
   :part on-irc
   :quit on-irc
   :366 (fn [server msg] (on-irc server (assoc msg :target (-> msg :params second))))
   })

(defn -main
  [& args]
  (parse-opts args)
  (load-state)
  (println @opts)
  (let [port (getopt :port)
        host (getopt :server)
        nick (first (getopt :nick))
        join (getopt :join)
        _ (println (str "Connecting to " host ":" port " as " nick))
        server (irc/connect host port nick :callbacks callbacks)]
    (println "Connected! Joining" join)
    (irc/join server join)
    (println "Done.")))
