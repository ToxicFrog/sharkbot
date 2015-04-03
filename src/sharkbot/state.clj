(ns sharkbot.state
  (:require [sharkbot.flags :refer :all]
            [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]
            ))

(def state (atom {:users {} :memory {}}))

(defn save-state []
  (println "Saving state:" (pr-str @state))
  (spit (getopt :persistence) (prn-str @state)))

(defn load-state []
  (if (.exists (io/as-file (getopt :persistence)))
    (do
      (reset! state (edn/read-string (slurp (getopt :persistence))))
      (println "Loading state:" (pr-str @state)))
    (println "Skipping state load -- persistence file missing.")))

(defn update-state [state']
  (if (not= @state state')
    (do
      (reset! state state')
      (save-state))))
