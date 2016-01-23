(ns sharkbot.modules.admin
  (:require
    [sharkbot.flags :refer [getopt]]
    [sharkbot.state :refer :all]
    [sharkbot.triggers :refer :all]
    [sharkbot.irc :refer :all]
    ))

(deftriggers die [capa _]
  "Shut down the bot."
  [(command "die")]
  (when ((getopt :admin) capa)
    (System/exit 0)))

(deftriggers raw [capa message]
  "Emit a raw IRC command."
  [(command "raw")]
  (when ((getopt :admin) capa)
    (let [message (apply str (interpose " " message))]
      (send-irc message))))

(deftriggers reload-state [capa _]
  "Force an immediate reload of state from disk."
  [(command "reload-state")]
  (when ((getopt :admin) capa)
    (reply "Loading state.")
    (load-state)))
