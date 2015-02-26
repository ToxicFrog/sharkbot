(ns sharkybot2.core
  (:require [irclj.core :as irc]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [clojure.tools.reader.edn :as edn]
            [clojure.java.io :as io])
  (:gen-class))

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

; :users { ToxicFrog { :pronouns :m :shark-time (datetime) :spoilers :RoT }}
; :spoilers { :LoLL 0 :RSURS 0 :RoT 3 }
(def state (atom {:users {}}))
(def books ["none" "LoLL" "RSURS" "RoT"])
(def spoiler-level (atom "RoT"))

(defn save-state [state]
  (println "Saving state:" (pr-str state))
  (spit (getopt :persistence) (prn-str state)))

(defn load-state []
  (if (.exists (io/as-file (getopt :persistence)))
    (do
      (reset! state (edn/read-string (slurp (getopt :persistence))))
      (println "Loading state:" (pr-str @state)))
    (println "Skipping state load -- persistence file missing.")))

(def ^:dynamic *irc* nil)
(def ^:dynamic *msg* nil)

(defn pronoun [nick]
  (-> @state
      :users
      (get nick {})
      :pronouns
      {:m "his" :f "her" :t "their"
       "m" "his" "f" "her" "t" "their"}
      (or "their")))

(defn reply [& text]
  (apply irc/reply *irc* *msg* text))

(defn get-user [nick]
  (-> @state :users (get nick {})))

(defn update-spoiler-level []
  (let [target (*msg* :target)
        names (keys (get-in @*irc* [:channels target :users]))
        levels (set (map (fn [name] (:spoilers (get-user name))) names))
        level (or (some levels books) @spoiler-level)]
    (prn levels)
    (prn level)
    (prn @spoiler-level)
    (if (not= level @spoiler-level)
      (do
        (reply (str "The spoiler level is now " level "."))
        (reset! spoiler-level level)))
    @state))

(defn update-user [nick k v]
  (let [user (get-user nick)]
    (assoc-in @state [:users nick k] v)))

(defn eat-victim [victim]
  (prn "EAT" victim)
  (reply "ACTION drags" victim "beneath the waves, swimming around for a while before spitting" (pronoun victim) "mangled remains back out.")
  @state)

(defn user-info [nick & _]
  (if (get-user nick)
    (do (reply (pr-str (get-user nick))) @state)
    @state))

(defn keyify [kvs]
  (->> kvs
       (partition 2)
       (mapcat (fn [kv] [(keyword (first kv)) (second kv)]))))

(defn set-fields [nick & kvs]
  (prn "SET" nick (keyify kvs))
  (let [state'
        (assoc-in @state [:users nick]
                  (apply assoc (get-user nick) (keyify kvs)))]
    (reply "Done.")
    state'))

(defn to-command [text]
  (let [nick (:nick @*irc*)]
    (cond
      (.startsWith text (str nick ", ")) (drop 1 (string/split text #"\s+")) ; Sharky, command args
      (.startsWith text "!") (-> text (subs 1) (string/split #"\s+")) ; !command args
      :else [nil ""])))

; should respond to both "!command args" and "Sharky2, command args"
; and also specific trigger text like "/me throws %s to the sharks"
; and "/me sends %s for teeth lessons"
(defn on-privmsg [nick user text]
  (let [[command & args] (to-command text)
        args (map #(string/replace % #"[.!,;]" "") args)]
    (case command
      ("teeth" "eat") (apply eat-victim args)
      "set" (apply set-fields nick args)
      "info" (apply user-info args)
      @state)))

(defn on-irc [server msg]
  (let [{text :text
         target :target
         nick :nick
         user :user
         command :command} msg]
    (binding [*irc* server
              *msg* msg]
      (try
        (let [state'
              (case command
                "PRIVMSG" (on-privmsg nick user text)
                "JOIN"    (update-spoiler-level)
                "PART"    (update-spoiler-level)
                "QUIT"    (update-spoiler-level)
                "366"     (update-spoiler-level)
                @state)]
          (if (not= @state state')
            (do
              (save-state state')
              (reset! state state'))))
        (catch Exception e
          (println "Error executing command:" text)
          (println "  >>" (.getMessage e)))))))

(def callbacks
  {:privmsg on-irc
   :join (fn [server msg] (on-irc server (assoc msg :target (-> msg :params first))))
   :part on-irc
   :quit on-irc
   :366 (fn [server msg] (on-irc server (assoc msg :target (-> msg :params second))))
   })

(defn -main
  [& args]
  (reset! opts (cli/parse-opts args flags))
  (println @opts)
  (load-state)
  (let [port (getopt :port)
        host (getopt :server)
        nick (getopt :nick)
        join (getopt :join)
        _ (println (str "Connecting to" host ":" port " as " nick))
        server (irc/connect host port nick :callbacks callbacks)]
    (println "Connected! Joining" join)
    (irc/join server join)
    (println "Done.")))
