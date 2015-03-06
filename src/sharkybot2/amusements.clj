(ns sharkybot2.core
  (:require
    [sharkybot2.users :refer :all]
    [sharkybot2.irc :refer :all]
    [sharkybot2.triggers :refer :all]
    ))

; Fun triggers

(deftriggers eat-victim [capa victim & args]
  "Send someone for teeth lessons."
  [(command "teeth")
   (command "eat")
   (action #"feeds (\S+) to the shark")
   (action #"sends (\S+) for teeth lessons")]
  (reply "ACTION drags" (user-name victim)
         "beneath the waves, swimming around for a while before spitting"
         (pronoun victim) "mangled remains back out."))

(deftriggers purr [& _]
  "It's a purring shark."
  [(action (re-pattern (str "(?i)pets " (nick-re))))]
  (reply "ACTION purrs."))

(deftriggers greet-newbie [capa & args]
  "Link a newbie to the newbie guide."
  [(command "newbie")
   (message #"there's a newbie()")
   (message #"(\S+) is a newbie")]
  (let [nick (if (empty? args) "the newbie" (first args))]
    (reply "\001ACTION gently chomps" (user-name nick) "and links to the newbie guide: http://goo.gl/4f2p0T\001")))

(deftriggers hug [capa & args]
  "Hug the shark! It's safe!"
  [(command "hug")
   (action (re-pattern (str "(?i)()hugs " (nick-re))))]
  (let [victim (first args)]
    (reply "\001ACTION nuzzles" (user-name (if (empty? victim) capa victim)) "gently.\001")))
