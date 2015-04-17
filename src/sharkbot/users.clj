(ns sharkbot.users
  (:require [sharkbot.flags :refer :all]
            [sharkbot.state :refer :all]
            [sharkbot.irc :refer :all]
            [clojure.string :as string]
            ))

(defn canonical-name [nick]
  (let [userinfo (-> @state :users (get nick))
        aliased (->> @state :users (filter #(-> % second :aliases (contains? nick))) (map first))]
    (cond
      userinfo nick
      (= 1 (count aliased)) (first aliased)
      :else nick)))

; Get info for a user.
; If the user is known, either directly or by alias, return that user.
; If there are multiple users known by this alias, returns nil after
; reporting an error to IRC.
; If the user is unknown, returns {}.
(defn get-user
  [nick]
  (let [userinfo (-> @state :users (get nick))
        aliased (->> @state :users (filter #(-> % second :aliases (contains? nick))) (map first))]
    (cond
      userinfo userinfo
      (= 1 (count aliased)) (get-user (first aliased))
      (< 1 (count aliased)) (do (reply "I know many people with that name:"
                                       (string/join ", " aliased))
                              nil)
      :else {})))

(def ^:private pronoun-map
  {:male #{"m" "male" "he" "him"}
   :female #{"f" "female" "she" "her"}
   :neuter #{"t" "they" "them"}
   :robot #{"it" "robot"}})

(defn- setting-to-gender [setting]
  (or
    (some (fn [[k v]] (when (v setting) k)) pronoun-map)
    :neuter))

; Returns the preferred posessive pronoun of the user. Defaults to "their".
(defn pronoun [nick]
  (-> (get-user nick)
      (get :pronouns "t")
      .toLowerCase
      setting-to-gender
      {:male "his" :female "her" :neuter "their" :robot "its"}))

; Returns the preferred name of the given user. If the user is unknown or
; has no preferred name, returns nick.
(defn user-name [nick]
  (or (:name (get-user nick))
      nick))

(defn update-user [nick f]
  (update-in @state [:users (canonical-name nick)] f))
