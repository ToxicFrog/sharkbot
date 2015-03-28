(ns sharkybot2.core
  (:require
    [sharkybot2.users :refer :all]
    [sharkybot2.irc :refer :all]
    [sharkybot2.triggers :refer :all]
    ))

; Fun triggers

(deftriggers eat-victim [capa [victim]]
  "Send someone for teeth lessons."
  [(command "teeth")
   (command "eat")
   (action #"feeds (.*) to the shark")
   (action #"sends (.*) for teeth lessons")]
  (reply "\001ACTION drags" (user-name victim)
         "beneath the waves, swimming around for a while before spitting"
         (pronoun victim) "mangled remains back out.\001"))

(deftriggers purr [_ _]
  "It's a purring shark."
  [(action (re-pattern (str "(?i)pets " (nick-re))))]
  (reply "\001ACTION purrs.\001"))

(deftriggers greet-newbie [capa [newbie]]
  "Link a newbie to the newbie guide."
  [(command "newbie")
   (message #"there's a newbie")
   (message #"(\S+) is a newbie")
   (message #"^\S+ (.*) are newbies")]
  (let [nick (or newbie "the newbie")]
    (reply "\001ACTION gently chomps" (user-name nick) "and links to the newbie guide: http://goo.gl/4f2p0T\001")))

(deftriggers hug [capa [victim]]
  "Hug the shark! It's safe!"
  [(command "hug")
   (action (re-pattern (str "(?i)hugs " (nick-re))))]
  (reply "\001ACTION nuzzles" (user-name (or victim capa)) "gently.\001"))

(deftriggers thanks [capa _]
  "Sharky should do something when you thank him."
  [(say (re-pattern (str "^(?i)(thanks|thank you|thank u).+" (nick-re))))
   (message #"(thanks|thank you|thank u)")]
  (reply "\001ACTION bloops.\001"))
