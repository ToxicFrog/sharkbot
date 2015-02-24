(ns sharkybot2.core
  (:require [irclj.core :as irc]
            [clojure.string :as string])
  (:gen-class))

; :users { ToxicFrog { :pronouns :m :shark-time (datetime) :spoilers :RoT }}
; :spoilers { :LoLL 0 :RSURS 0 :RoT 3 }
(def state (atom {:users {} :spoilers {}}))

(def ^:dynamic *state* nil)
(def ^:dynamic *irc* nil)
(def ^:dynamic *msg* nil)

(defn pronoun [nick]
  (-> *state*
      :users
      (get nick {})
      :pronouns
      {:m "his" :f "her" :t "their"}
      (or "their")))

(defn reply [& text]
  (apply irc/reply *irc* *msg* text))

(defn update-user [nick k v]
  (let [user (-> *state* :users (get nick {}))]
    (assoc-in *state* [:users nick k] v)))

; If the user has a spoiler level set, inc the spoiler refcount.
; If this results in a lower spoiler level, report it.
; If the user has an expired sharktimer, report that, clear the timer, and
; save state.
(defn on-join [nick user]
  (prn (str nick "!" user) "JOIN")
  *state*)

; If the user has a spoiler level set, dec the spoiler refcount.
; If this results in a higher spoiler level, report it.
(defn on-quit [nick user]
  (prn (str nick "!" user) "QUIT")
  *state*)

(defn eat-victim [victim]
  (prn "EAT" victim)
  (reply "ACTION drags" victim "beneath the waves, swimming around for a while before spitting" (pronoun victim) "mangled remains back out.")
  *state*)

(defn set-pronouns [nick pronouns]
  (prn "PRONOUNS" nick pronouns)
  (let [pronouns (keyword pronouns)
        known-pronouns #{:m :f :t}]
    (if (known-pronouns pronouns)
      (do
        (reply "Done.")
        (update-user nick :pronouns pronouns))
      *state*)))

(defn set-spoilers [nick spoilers]
  (prn "SPOILERS" nick spoilers))

(defn to-command [text]
  (let [nick (:nick @*irc*)]
    (cond
      (.startsWith text (str nick ", ")) (drop 1 (string/split text #"\s+" 4)) ; Sharky, command args
      (.startsWith text "!") (-> text (subs 1) (string/split #"\s+" 3)) ; !command args
      :else [nil ""])))

; should respond to both "!command args" and "Sharky2, command args"
; and also specific trigger text like "/me throws %s to the sharks"
; and "/me sends %s for teeth lessons"
(defn on-privmsg [nick user text]
  (let [[command args] (take 2 (to-command text))
        args (-> args (string/replace #"[.!,;]" " ") (string/trim))]
    (case command
      ("teeth" "eat") (eat-victim args)
      "pronouns" (set-pronouns nick args)
      "spoilers" (set-spoilers nick args)
      *state*)))

(defn on-irc [server msg]
  (let [{text :text
         target :target
         nick :nick
         user :user
         command :command} msg]
    (binding [*state* @state
              *irc* server
              *msg* msg]
      (let [state'
            (case command
              "PRIVMSG" (on-privmsg nick user text)
              "JOIN"    (on-join nick user)
              "PART"    (on-quit nick user)
              "QUIT"    (on-quit nick user)
              *state*)]
        (reset! state state')))))

(def callbacks
  {:privmsg on-irc
   :join on-irc
   :part on-irc
   :quit on-irc})

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [server (irc/connect "irc.freenode.net" 6667 "Sharky2" :callbacks callbacks)]
    (irc/join server "#gbchat")
    (println "Done connecting to IRC.")))
