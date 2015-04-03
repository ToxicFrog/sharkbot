(ns sharkbot.irc
  (:require
    [irclj.core :as irc]
    [irclj.connection :refer [write-irc-line]]
    ))

(def ^:dynamic *irc* nil)
(def ^:dynamic *msg* nil)

(defn reply [& text]
  (apply irc/reply *irc* *msg* text))

(defn send-irc [& text]
  (apply write-irc-line *irc* text))
