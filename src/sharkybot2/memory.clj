(ns sharkybot2.memory
  (:require
    [sharkybot2.state :refer :all]
    [sharkybot2.irc :refer :all]
    [sharkybot2.triggers :refer :all]
    [clojure.string :as string]
    ))

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

