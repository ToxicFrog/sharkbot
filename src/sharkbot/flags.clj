(ns sharkbot.flags
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string]))

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
   ["-n" "--nick NICKS" "Comma separated list of names to recognize; first will be used as IRC nick"
    :default ["SharkyMcJaws" "sharky"]
    :parse-fn #(string/split % #",")]
   ["-j" "--join CHANNEL" "IRC channel to join"
    :default "#gbchat"]
   ["-a" "--admin USER,..." "Names of users to recognize as administrators"
    :default #{"ToxicFrog"}
    :parse-fn #(set (string/split % #","))]
   ["-P" "--persistence FILE" "Save bot state in this file"
    :default "sharky.edn"]
   ["-h" "--help" "Display help"]])

(defn parse-opts [args]
  (reset! opts (cli/parse-opts args flags)))

(defn nick []
  (first (getopt :nick)))

(defn nick-re []
  (str "(?:"
    (->> (getopt :nick) (map #(.toLowerCase %)) (interpose "|") (apply str))
    ")"))
