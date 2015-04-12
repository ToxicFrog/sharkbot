(ns sharkbot.modules.admin
  (:require
    [sharkbot.flags :refer [getopt]]
    [sharkbot.triggers :refer :all]
    ))

(deftriggers die [capa _]
  "Shut down the bot."
  [(command "die")]
  (when ((getopt :admin) capa)
    (System/exit 0)))
