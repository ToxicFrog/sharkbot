(ns sharkybot2.core
  (:require [sharkybot2.state :refer :all]
            [sharkybot2.flags :refer :all]
            [irclj.core :as irc]
            [clojure.string :as string]
            )
  (:gen-class))

(def books ["none" "LoLL" "RSURS" "RoT"])
(def spoiler-level (atom "RoT"))

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

(defn update-spoiler-level
  ([] (update-spoiler-level false))
  ([force-display]
    (let [target (*msg* :target)
          names (keys (get-in @*irc* [:channels target :users]))
          levels (set (map (fn [name] (:spoilers (get-user name))) names))
          level (or (some levels books) @spoiler-level)]
      (println "Scanned" (pr-str names) "and decided on a spoiler level of " level)
      (if (or force-display (not= level @spoiler-level))
        (do
          (reply (str "The spoiler level is now " level "."))
          (reset! spoiler-level level))))))

(defn spoilers-command [& _]
  (update-spoiler-level true))

(defn update-spoilers [& _]
  (update-spoiler-level false))

(defn eat-victim [capa victim & _]
  (prn "EAT" victim)
  (reply "ACTION drags" victim "beneath the waves, swimming around for a while before spitting" (pronoun victim) "mangled remains back out."))

(defn user-info [nick & _]
  (if (get-user nick)
    (do (reply (pr-str (get-user nick))) @state)))

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
    (update-state state')
    (update-spoiler-level)))

(defn to-command [server text]
  (let [nick (:nick @server)]
    (cond
      (nil? text) [nil nil]
      (.startsWith text (str nick ", ")) (drop 1 (string/split text #"\s+")) ; Sharky, command args
      (.startsWith text "!") (-> text (subs 1) (string/split #"\s+")) ; !command args
      :else [nil nil])))

(defn parse-msg [server msg]
  (let [[command & args] (to-command server (:text msg))]
    (assoc msg
      :bot-command command
      :bot-args args)))

(defn command
  "True if *msg* is the given bot command."
  [cmd]
  (= cmd (:bot-command *msg*)))

(defn action
  "True if *msg* is a CTCP ACTION matching regex."
  [regex]
  false)

(defn message
  "True if *msg* is a channel message directed at the bot and matching regex"
  [regex]
  false)

(defn raw
  "True if the raw IRC command or numeric matches."
  [cmd]
  (= cmd (:command *msg*)))

(defn call-handler [msg handler]
  (if handler
    (do (prn "Calling event handler" handler)
      (apply handler (:nick msg) (:bot-args msg)))))

(defn on-irc [server msg]
  (binding [*irc* server
            *msg* (parse-msg server msg)]
    (try
      (call-handler *msg* (cond
        ; Teeth lessons
        (command "teeth") eat-victim
        (command "eat")   eat-victim
        (action #"feeds (\S+) to the shark")      eat-victim
        (action #"sends (\S+) for teeth lessons") eat-victim

        ; User info
        (command "set")   set-fields
        (command "info")  user-info
        (message #"tell me about (\S+)") user-info
        ; (command "alias") add-aliases
        ; (command "unalias") rm-aliases

        ; Spoiler management
        (command "spoilers") spoilers-command
        (message #"check the spoiler level")  spoilers-command
        (message #"what's the spoiler level") spoilers-command
        (raw "JOIN") update-spoilers
        (raw "PART") update-spoilers
        (raw "QUIT") update-spoilers
        (raw "366")  update-spoilers

        ; Purring shark
        ; (action (re-pattern (str "pets " (@opts :nick)))) purr
        :else nil))
      (catch Exception e
        (println "Error executing command:" (:raw *msg*))
        (println "  >>" (.getMessage e))))))

(def callbacks
  {:privmsg on-irc
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
        nick (getopt :nick)
        join (getopt :join)
        _ (println (str "Connecting to" host ":" port " as " nick))
        server (irc/connect host port nick :callbacks callbacks)]
    (println "Connected! Joining" join)
    (irc/join server join)
    (println "Done.")))
