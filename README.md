# sharkybot2

A simple IRC bot for #gbchat, the Gentleman Bastards fan channel. Remembers pronouns and spoiler levels and eats people.

## Installation

TODO

## Usage

`lein run`. Connects and joins automatically. TODO: specify IRC info on command line.

## Roadmap

- state persistence
- trigger on privmsg events other than !command or <name>, command
- proper command line parsing and configurable name/channel/network info
- proper logging
- remember per-user aliases
- remember per-user spoilers
- !set [k v & kvs] to set info
- !alias [name] to mark current nick as an alias
- !info [name] and 'Sharky, tell me about [name]' to get user info
- !spoilers to get current spoiler level

 @Ariaste | "Sharky, there's a newbie"                                                         
 @Ariaste | "Sharky gently chomps the newbie and links them to the newbie's guide (LINK HERE)" 

/me pets sharky -> sharky purrs

(triggers
  (action "pets $N") purr
  (action "sends $T for teeth lessons") eat
  (command "eat") eat
  (command "set") set
  (command "info") info
  (message "tell me about $T") info
  ...)

## License

Copyright © 2015 Ben 'ToxicFrog' Kelly

Released under the MIT license; see the LICENSE file for details.
