(ns sharkbot.modules.userinfo
  (:require
    [sharkbot.state :refer :all]
    [sharkbot.users :refer :all]
    [sharkbot.irc :refer :all]
    [sharkbot.triggers :refer :all]
    [sharkbot.modules.spoilers :refer [update-spoiler-level]]
    [clojure.string :as string]
    ))

; User info management

(deftriggers info [capa [nick]]
  "Show information about a user."
  [(command "info")
   (message #"tell me about (\S+)")
   (message #"what do you know about me")]
  (let [nick (or nick capa)
        info (get-user nick)]
    (cond
      (nil? info) nil
      (empty? info) (reply "I have no knowledge of that person.")
      :else (reply (str nick ":") (pr-str (get-user nick))))))

(deftriggers set [nick [key & val]]
  "Set user info."
  [(command "set")]
  (let [key (-> key .toLowerCase keyword)
        val (string/join " " val)
        state' (if (empty? val)
                 (update-user nick #(dissoc %1 key))
                 (update-user nick #(assoc %1 key val)))]
    (update-state state')
    (reply "Done.")
    (update-spoiler-level)))

(deftriggers unset [nick ks]
  "Clear user info."
  [(command "unset")
   (command "clear")]
  (let [state' (update-user nick #(apply dissoc % (map keyword ks)))]
    (reply "Done.")
    (update-state state')
    (update-spoiler-level)))
