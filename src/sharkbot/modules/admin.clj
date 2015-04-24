(ns sharkbot.modules.admin
  (:require
    [sharkbot.flags :refer [getopt]]
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
