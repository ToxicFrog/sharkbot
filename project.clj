(defproject sharkybot2 "0.1.0-SNAPSHOT"
  :description "Sends people for teeth lessons."
  :url "irc://irc.freenode.net/gbchat"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [irclj "0.5.0-alpha4"]
                 [org.clojure/tools.cli "0.3.1"]]
  :main sharkybot2.core)
