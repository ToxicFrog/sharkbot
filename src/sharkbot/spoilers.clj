(ns sharkbot.spoilers
  (:require
    [sharkbot.users :refer :all]
    [sharkbot.irc :refer :all]
    [sharkbot.flags :refer [getopt]]
    [sharkbot.triggers :refer :all]
    [clojure.string :as string]
    ))

; Spoiler level control

(def ^:private spoiler-level (atom "RoT"))
(def ^:private books
  [["NO SPOILERS" #{"none" "nothing" "no"}]
   ["Lies of Locke Lamora" #{"lies" "loll" "tloll"}]
   ["Red Seas Under Red Skies" #{"seas" "rsurs"}]
   ["Republic of Thieves" #{"republic" "rot" "trot"}]
   ["Thorn of Emberlain" #{"thorn" "toe"}]
   ])

(defn- level-to-book-name [level]
  (some (fn [[book levels]]
          (when (levels (.toLowerCase level)) book))
        books))

(defn update-spoiler-level
  ([] (update-spoiler-level false))
  ([force-display]
    (let [target (or (*msg* :target) (getopt :join))
          names (keys (get-in @*irc* [:channels target :users]))
          levels (->> names (map get-user) (map :spoilers) (filter identity) (map level-to-book-name) set)
          level (or (some levels (map first books)) @spoiler-level)
          topic (get-in @*irc* [:channels target :topic :text])
          new-topic (string/replace (or topic "") #"CURRENT SPOILER LEVEL: .*" (str "CURRENT SPOILER LEVEL: \002" level "\002"))]
      (println "Scanned" (pr-str names) "and decided on a spoiler level of" level)
      (if (or force-display (not= level @spoiler-level))
        (do
          (reply (str "The spoiler level is now \002" level "\002."))
          (when (and (not= topic new-topic) (not (empty? new-topic)))
            (send-irc "TOPIC" target (str ":" new-topic)))
          (reset! spoiler-level level))))))

(deftriggers show-spoiler-level [_ _]
  "Explicitly check and show the spoiler level."
  [(command "spoilers")
   (message #"check the spoiler level")
   (message #"what's the spoiler level")]
  (update-spoiler-level true))

(deftriggers check-spoiler-level [_ _]
  "Automatically check the spoiler level and show an update only if it changed."
  [(raw "JOIN")
   (raw "PART")
   (raw "366")]
  (update-spoiler-level false))

(deftriggers rescan-users [_ _]
  "Force a rescan of the user list."
  [(raw "QUIT")]
  (dosync (alter *irc*
                 assoc-in [:channels (getopt :join) :users] {}
                 ))
  (send-irc "NAMES" (getopt :join)))

