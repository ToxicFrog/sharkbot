(ns sharkybot2.flags
  (:require [clojure.tools.cli :as cli]))

(def opts (atom nil))
(defn getopt [key]
  (-> @opts :options key))
(def flags
  [["-s" "--server HOST" "IRC server to connect to"
    :default "irc.freenode.net"]
   ["-p" "--port PORT" "Port number of IRC server"
    :default 6667
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-n" "--nick NICK" "Nickname to use on IRC"
    :default "SharkyMcJaws"]
   ["-j" "--join CHANNELS" "Comma-separated list of channels to join"
    :default "#gbchat"]
   ["-P" "--persistence FILE" "Save bot state in this file"
    :default "sharky.edn"]])

(defn parse-opts [args]
  (reset! opts (cli/parse-opts args flags)))
