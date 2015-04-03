(ns sharkbot.userinfo
  (:require
    [sharkbot.state :refer :all]
    [sharkbot.users :refer :all]
    [sharkbot.irc :refer :all]
    [sharkbot.triggers :refer :all]
    [sharkbot.spoilers :refer [update-spoiler-level]]
    ))

; User info management

(deftriggers info [capa [nick]]
  "Show information about a user."
  [(command "info")
   (message #"tell me about (\S+)")]
  (let [info (get-user nick)]
    (cond
      (nil? info) nil
      (empty? info) (reply "I have no knowledge of that person.")
      :else (reply (str (canonical-name nick) ":") (pr-str (get-user nick))))))

(defn- keyify [kvs]
  (->> kvs
       (partition 2)
       (mapcat (fn [kv] [(keyword (first kv)) (second kv)]))))

(deftriggers set [nick kvs]
  "Set user info."
  [(command "set")]
  (let [update (dissoc (apply assoc {} (keyify kvs)) :aliases)
        state' (update-user nick #(conj (or % {}) update))]
    (reply "Done.")
    (update-state state')
    (update-spoiler-level)))

(deftriggers unset [nick ks]
  "Clear user info."
  [(command "unset")]
  (let [state' (update-user nick #(apply dissoc % (map keyword ks)))]
    (reply "Done.")
    (update-state state')
    (update-spoiler-level)))

(deftriggers alias [nick aliases]
  "Add aliases."
  [(command "alias")]
  (let [state' (update-user nick #(assoc %1 :aliases (apply conj (or (:aliases %1) #{}) aliases)))]
    (update-state state')
    (reply "Done.")))

(deftriggers unalias [nick aliases]
  "Remove aliases."
  [(command "unalias")]
  (let [state' (update-user nick #(assoc %1 :aliases (apply disj (:aliases %1) aliases)))]
    (update-state state')
    (reply "Done.")))


