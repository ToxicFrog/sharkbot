(ns sharkbot.modules.memory
  (:require
    [sharkbot.state :refer :all]
    [sharkbot.irc :refer :all]
    [sharkbot.triggers :refer :all]
    [clojure.string :as string]
    ))

; Memory

(deftriggers remember [capa [key & vs]]
  "Remember a new fact, or show an old one."
  [(command "remember")]
  (if (nil? key)
    (reply "Memories:" (-> @state :memory keys pr-str))
    (let [k (-> key .toLowerCase keyword)]
      (if (empty? vs)
        (reply (str key ":") (get-in @state [:memory k]))
        (let [state' (assoc-in @state [:memory k] (string/join " " vs))]
          (update-state state')
          (reply "Remembered."))))))

(deftriggers forget [capa [key]]
  "Forget a remembered fact."
  [(command "forget")]
  (let [key (-> key .toLowerCase keyword)
        state' (update-in @state [:memory] #(dissoc % key))]
    (update-state state')
    (reply "Forgotten.")))
