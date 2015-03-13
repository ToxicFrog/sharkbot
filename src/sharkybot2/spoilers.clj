(ns sharkybot2.spoilers
  (:require
    [sharkybot2.users :refer :all]
    [sharkybot2.irc :refer :all]
    [sharkybot2.flags :refer [getopt]]
    [sharkybot2.triggers :refer :all]
    ))

; Spoiler level control

(def ^:private books ["none" "LoLL" "RSURS" "RoT"])
(def ^:private spoiler-level (atom "RoT"))

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

