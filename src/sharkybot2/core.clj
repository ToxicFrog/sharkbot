(ns sharkybot2.core
  (:require [sharkybot2.state :refer :all]
            [sharkybot2.flags :refer :all]
            [sharkybot2.users :refer :all]
            [sharkybot2.irc :refer :all]
            [irclj.core :as irc]
            [clojure.string :as string]
            [clojure.stacktrace :as trace]
            )
  (:gen-class))



; Spoiler level control

(def books ["none" "LoLL" "RSURS" "RoT"])
(def spoiler-level (atom "RoT"))

(defn update-spoiler-level
  ([] (update-spoiler-level false))
  ([force-display]
    (let [target (or (*msg* :target) (getopt :join))
          names (keys (get-in @*irc* [:channels target :users]))
          levels (->> names (map get-user) (map :spoilers) set)
          level (or (some levels books) @spoiler-level)]
      (println "Scanned" (pr-str names) "and decided on a spoiler level of" level)
      (if (or force-display (not= level @spoiler-level))
        (do
          (reply (str "The spoiler level is now " level "."))
          (reset! spoiler-level level))))))

(defn spoilers-command [& _]
  (update-spoiler-level true))

(defn update-spoilers [& _]
  (update-spoiler-level false))


; Fun triggers

(defn eat-victim [capa victim & _]
  (reply "ACTION drags" (user-name victim)
         "beneath the waves, swimming around for a while before spitting"
         (pronoun victim) "mangled remains back out."))

(defn purr [& _]
  (reply "ACTION purrs."))

(defn newbie-intro [capa nick &]
  (let [nick (if (empty? nick) "the newbie" nick)]
    (reply "\001ACTION gently chomps" (user-name nick) "and links to the newbie guide: http://goo.gl/4f2p0T\001")))

(defn hug [capa victim & _]
  (reply "\001ACTION nuzzles" (user-name victim) "gently.\001"))


; User info management

(defn user-info [capa nick & _]
  (let [info (get-user nick)]
    (cond
      (nil? info) nil
      (empty? info) (reply "I have no knowledge of that person.")
      :else (reply (str (canonical-name nick) ":") (pr-str (get-user nick))))))

(defn keyify [kvs]
  (->> kvs
       (partition 2)
       (mapcat (fn [kv] [(keyword (first kv)) (second kv)]))))

(defn set-fields [nick & kvs]
  (let [update (dissoc (apply assoc {} (keyify kvs)) :aliases)
        state' (update-user nick #(conj (or % {}) update))]
    (reply "Done.")
    (update-state state')
    (update-spoiler-level)))

(defn unset-fields [nick & ks]
  (let [state' (update-user nick #(apply dissoc % (map keyword ks)))]
    (reply "Done.")
    (update-state state')
    (update-spoiler-level)))

(defn add-aliases [nick & aliases]
  (let [state' (update-user nick #(assoc %1 :aliases (apply conj (or (:aliases %1) #{}) aliases)))]
    (update-state state')
    (reply "Done.")))

(defn rm-aliases [nick & aliases]
  (let [state' (update-user nick #(assoc %1 :aliases (apply disj (:aliases %1) aliases)))]
    (update-state state')
    (reply "Done.")))


; Memory

(defn remember-info [capa key & vs]
  (let [k (-> key .toLowerCase keyword)]
    (if (empty? vs)
      (reply (str key ":") (get-in @state [:memory k]))
      (let [state' (assoc-in @state [:memory k] (string/join " " vs))]
        (update-state state')
        (reply "Remembered.")))))

(defn forget-info [capa k]
  (let [k (-> k .toLowerCase keyword)
        state' (update-in @state [:memory] #(dissoc % k))]
    (update-state state')
    (reply "Forgotten.")))


; Command parsing

(defn to-command [server text]
  (let [nick (:nick @server)]
    (cond
      (nil? text) [nil nil]
      (.startsWith text nick) (drop 1 (string/split text #"\s+")) ; Sharky, command args
      (.startsWith text "!") (-> text (subs 1) (string/split #"\s+")) ; !command args
      :else [nil nil])))

(defn parse-msg [server msg]
  (let [[command & args] (to-command server (:text msg))]
    [command (or args[])]))

(defn groups [re str]
  (let [m (re-matcher re str)]
    (re-find m)
    (drop 1 (re-groups m))))

(defn command
  "True if *msg* is the given bot command."
  [cmd]
  (let [[msg-cmd args] (parse-msg *irc* *msg*)]
    (if (= cmd msg-cmd)
      args)))

(defn action
  "True if *msg* is a CTCP ACTION matching regex."
  [regex]
  (if (= "ACTION" (:ctcp-kind *msg*))
    (let [m (re-matcher regex (:ctcp-text *msg*))]
      (and (re-find m)
           (drop 1 (re-groups m))))))

(defn message
  "True if *msg* is a channel message directed at the bot and matching regex"
  [regex]
  (if (= "PRIVMSG" (:command *msg*))
    (do
      (let [m (re-matcher regex (:text *msg*))]
        (and (.startsWith (:text *msg*) (getopt :nick))
             (re-find m)
             (drop 1 (re-groups m)))))))

(defn raw
  "True if the raw IRC command or numeric matches."
  [cmd]
  (if (= cmd (:command *msg*))
    []
    false))

(defn find-handler [cond msg]
  (cond))

(defn call-handler [handler args]
  (prn "CALL" handler args)
  (apply handler (:nick *msg*) args))

; Take a sequence of (matcher handler matcher handler ...)
(defmacro handlers [& hs]
  (let [hs (->> hs (partition 2) (mapcat (fn [[m h]] `((fn [] ~m) :>> (partial call-handler ~h)))))]
    `(condp find-handler *msg* ~@hs nil)))

(defn on-irc [server msg]
  (binding [*irc* server
            *msg* msg]
    (try
      (handlers
        ; Teeth lessons
        (command "teeth") eat-victim
        (command "eat")   eat-victim
        (action #"feeds (\S+) to the shark")      eat-victim
        (action #"sends (\S+) for teeth lessons") eat-victim

        ; User info
        (command "set")   set-fields
        (command "unset") unset-fields
        (command "info")  user-info
        (message #"tell me about (\S+)") user-info
        (command "alias") add-aliases
        (command "unalias") rm-aliases

        ; Memory
        (command "remember") remember-info
        (command "forget") forget-info

        ; Spoiler management
        (command "spoilers") spoilers-command
        (message #"check the spoiler level")  spoilers-command
        (message #"what's the spoiler level") spoilers-command
        (raw "JOIN") update-spoilers
        (raw "PART") update-spoilers
        (raw "QUIT") update-spoilers
        (raw "366")  update-spoilers

        ; Greeting
        (message #"there's a newbie()") newbie-intro
        (message #"(\S+) is a newbie") newbie-intro

        ; Purring shark
        (action (re-pattern (str "pets " (getopt :nick)))) purr
        (command "hug") hug
        nil))
      (catch Exception e
        (println "Error executing command:" (:raw *msg*))
        (trace/print-stack-trace e))))

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
        nick (getopt :nick)
        join (getopt :join)
        _ (println (str "Connecting to " host ":" port " as " nick))
        server (irc/connect host port nick :callbacks callbacks)]
    (println "Connected! Joining" join)
    (irc/join server join)
    (println "Done.")))
