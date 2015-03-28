(ns sharkybot2.triggers
  (:require [clojure.string :as string]
            [clojure.stacktrace :as trace]
            [sharkybot2.flags :refer :all]
            [sharkybot2.irc :refer :all]
            )
  (:gen-class))

(def command-triggers (atom {}))
(def help-topics (atom {}))

(defmacro deftriggers [name args help-text triggers & body]
  (let [triggers (mapv (fn [t] `(fn [] ~t)) triggers)]
    (println "Registering action" name "with" (count triggers) "triggers.")
    `(let [~name (fn ~args
                   ~@body)]
       (reset! command-triggers (assoc @command-triggers (str '~name)
                                  (mapv (fn [f#] [f# ~name]) ~triggers)))
       )))

(defn name-prefixed [text]
  (when (not (nil? text))
    (let [names (getopt :nick)
          text (.toLowerCase text)]
      (some #(.startsWith text %) (map #(.toLowerCase %) names)))))

(defn- to-command [server text]
  (cond
    (nil? text) [nil nil]
    (name-prefixed text) (drop 1 (string/split text #"\s+")) ; Sharky, command args
    (.startsWith text "!") (-> text (subs 1) (string/split #"\s+")) ; !command args
    :else [nil nil]))

(defn- parse-msg [server msg]
  (let [[command & args] (to-command server (:text msg))]
    [command (or args [])]))

(defn args-from-matcher [m]
  (cond
    (not (re-find m))     nil
    (= 0 (.groupCount m)) []
    :else                 (drop 1 (re-groups m))))

(defn command
  "True if *msg* is the given bot command. Produces one arg per word following the command."
  [cmd]
  (let [[msg-cmd args] (parse-msg *irc* *msg*)]
    (if (= cmd msg-cmd)
      args)))

(defn action
  "True if *msg* is a CTCP ACTION matching regex. Produces an arg for each capture."
  [regex]
  (if (= "ACTION" (:ctcp-kind *msg*))
    (let [m (re-matcher regex (:ctcp-text *msg*))]
      (args-from-matcher m))))

(defn message
  "True if *msg* is a channel message directed at the bot and matching regex. Produces an arg for each capture."
  [regex]
  (if (= "PRIVMSG" (:command *msg*))
    (do
      (let [m (re-matcher regex (:text *msg*))]
        (and (name-prefixed (:text *msg*))
             (args-from-matcher m))))))

(defn say
  "True if *msg* is a channel message matching regex. Produces an arg for each capture."
  [regex]
  (if (= "PRIVMSG" (:command *msg*))
    (do
      (let [m (re-matcher regex (:text *msg*))]
        (args-from-matcher m)))))

(defn raw
  "True if the raw IRC command or numeric matches. Produces no args."
  [cmd]
  (if (= cmd (:command *msg*))
    []
    false))

(defn on-irc [server msg]
  (binding [*irc* server
            *msg* msg]
    (try
      (let [find-handler (fn [[trigger handler]]
                           (let [words (trigger)]
                             (when words [words handler])))
            [args handler] (->> @command-triggers vals (apply concat) (some find-handler))]
        (when handler
          (prn "RAW " (:raw msg))
          (prn "CALL" handler args)
          ; Call handler with two arguments:
          ; the name of the person who initiated the message (may be nil in the case of e.g. server numerics)
          ; the processed message; for command/raw this is the result of splitting on whitespace, for regex-based
          ; triggers this is the set of groups
          (handler (:nick *msg*) args)))
      (catch Exception e
        (println "Error executing command:" (:raw *msg*))
        (trace/print-stack-trace e)
        (println "")
        ))))

(deftriggers hot-reload [capa modules]
  "Reload all user triggers."
  [(command "hot-reload")]
  (if (and ((getopt :admin) capa) (not (empty? modules)))
    (do
      (apply println "Admin hot-reload requested of modules:" modules)
      (apply require :reload (map #(symbol (str "sharkybot2." %)) modules)))
    (do
      (println "Hot-reload requested, reloading event handlers.")
      (require :reload
               '(sharkybot2 userinfo spoilers memory amusements core)))
    ))
