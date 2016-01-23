(ns sharkbot.users
  (:require [sharkbot.flags :refer :all]
            [sharkbot.state :refer :all]
            [sharkbot.irc :refer :all]
            [clojure.string :as string]
            ))

; Get info for a user. Returns {} if the user is unknown.
(defn get-user
  [nick]
  (-> @state :users (get (string/lower-case nick) {})))

; A user's *preferred name* is the name they have set as their :name. If they
; haven't set a :name, it's the name they're currently wearing.
(defn user-name [nick]
  (or (:name (get-user nick))
      nick))

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

(defn update-user [nick f]
  (update-in @state [:users nick] f))
