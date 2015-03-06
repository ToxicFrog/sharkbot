(ns sharkybot2.core
  (:require [sharkybot2.state :refer :all]
            [sharkybot2.flags :refer :all]
            [sharkybot2.users :refer :all]
            [sharkybot2.irc :refer :all]
            [sharkybot2.triggers :refer :all]
            [irclj.core :as irc]
            [irclj.connection :refer [write-irc-line]]
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

(deftriggers show-spoiler-level [& _]
  "Explicitly check and show the spoiler level."
  [(command "spoilers")
   (message #"check the spoiler level")
   (message #"what's the spoiler level")]
  (update-spoiler-level true))

(deftriggers check-spoiler-level [& _]
  "Automatically check the spoiler level and show an update only if it changed."
  [(raw "JOIN")
   (raw "PART")
   (raw "366")]
  (update-spoiler-level false))

(deftriggers rescan-users [& _]
  "Force a rescan of the user list."
  [(raw "QUIT")]
  (dosync (alter *irc*
                 assoc-in [:channels (getopt :join) :users] {}
                 ))
  (write-irc-line *irc* "NAMES" (getopt :join)))

; Fun triggers

(deftriggers eat-victim [capa victim & args]
  "Send someone for teeth lessons."
  [(command "teeth")
   (command "eat")
   (action #"feeds (\S+) to the shark")
   (action #"sends (\S+) for teeth lessons")]
  (reply "ACTION drags" (user-name victim)
         "beneath the waves, swimming around for a while before spitting"
         (pronoun victim) "mangled remains back out."))

(deftriggers purr [& _]
  "It's a purring shark."
  [(action (re-pattern (str "(?i)pets " (nick-re))))]
  (reply "ACTION purrs."))

(deftriggers greet-newbie [capa & args]
  "Link a newbie to the newbie guide."
  [(command "newbie")
   (message #"there's a newbie()")
   (message #"(\S+) is a newbie")]
  (let [nick (if (empty? args) "the newbie" (first args))]
    (reply "\001ACTION gently chomps" (user-name nick) "and links to the newbie guide: http://goo.gl/4f2p0T\001")))

(deftriggers hug [capa & args]
  "Hug the shark! It's safe!"
  [(command "hug")
   (action (re-pattern (str "(?i)()hugs " (nick-re))))]
  (let [victim (first args)]
    (reply "\001ACTION nuzzles" (user-name (if (empty? victim) capa victim)) "gently.\001")))


; User info management

(deftriggers info [capa nick & _]
  "Show information about a user."
  [(command "info")
   (message #"tell me about (\S+)")]
  (let [info (get-user nick)]
    (cond
      (nil? info) nil
      (empty? info) (reply "I have no knowledge of that person.")
      :else (reply (str (canonical-name nick) ":") (pr-str (get-user nick))))))

(defn keyify [kvs]
  (->> kvs
       (partition 2)
       (mapcat (fn [kv] [(keyword (first kv)) (second kv)]))))

(deftriggers set [nick & kvs]
  "Set user info."
  [(command "set")]
  (let [update (dissoc (apply assoc {} (keyify kvs)) :aliases)
        state' (update-user nick #(conj (or % {}) update))]
    (reply "Done.")
    (update-state state')
    (update-spoiler-level)))

(deftriggers unset [nick & ks]
  "Clear user info."
  [(command "unset")]
  (let [state' (update-user nick #(apply dissoc % (map keyword ks)))]
    (reply "Done.")
    (update-state state')
    (update-spoiler-level)))

(deftriggers alias [nick & aliases]
  "Add aliases."
  [(command "alias")]
  (let [state' (update-user nick #(assoc %1 :aliases (apply conj (or (:aliases %1) #{}) aliases)))]
    (update-state state')
    (reply "Done.")))

(deftriggers unalias [nick & aliases]
  "Remove aliases."
  [(command "unalias")]
  (let [state' (update-user nick #(assoc %1 :aliases (apply disj (:aliases %1) aliases)))]
    (update-state state')
    (reply "Done.")))


; Memory

(deftriggers remember [capa key & vs]
  "Remember a new fact, or show an old one."
  [(command "remember")]
  (let [k (-> key .toLowerCase keyword)]
    (if (empty? vs)
      (reply (str key ":") (get-in @state [:memory k]))
      (let [state' (assoc-in @state [:memory k] (string/join " " vs))]
        (update-state state')
        (reply "Remembered.")))))

(deftriggers forget [capa key & _]
  "Forget a remembered fact."
  [(command "forget")]
  (let [key (-> key .toLowerCase keyword)
        state' (update-in @state [:memory] #(dissoc % key))]
    (update-state state')
    (reply "Forgotten.")))

(deftriggers memories [capa & _]
  "List the shark's memories."
  [(command "memories")]
  (reply "Memories:" (-> @state :memory keys pr-str)))

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
