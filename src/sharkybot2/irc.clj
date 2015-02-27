(ns sharkybot2.irc
  (:require [irclj.core :as irc]))

(def ^:dynamic *irc* nil)
(def ^:dynamic *msg* nil)

(defn reply [& text]
  (apply irc/reply *irc* *msg* text))
