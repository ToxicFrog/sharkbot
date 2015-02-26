# sharkybot2

A simple IRC bot for #gbchat, the Gentleman Bastards fan channel. Remembers pronouns and spoiler levels and eats people.

## Installation

Download the jar from the releases page, or download the source and `lein run` or `lein uberjar`.

## Usage

`lein run`. Connects and joins automatically, but this can be customized on the command line. See `lein run --help` for details.

## Roadmap

- trigger on privmsg events other than !command or <name>, command
- proper logging
- remember per-user aliases
- !alias [name] to mark current nick as an alias
- !spoilers to get current spoiler level

 @Ariaste | "Sharky, there's a newbie"
 @Ariaste | "Sharky gently chomps the newbie and links them to the newbie's guide (LINK HERE)"

/me pets sharky -> sharky purrs

(triggers
  (command "teeth") eat-victim
  (command "eat")   eat-victim
  (action "feeds * to the shark") eat-victim
  (action "sends * for teeth lessons") eat-victim

  (command "set")   set-fields
  (command "info")  user-info
  (message "<> tell me about *") user-info
  (message "<> check the spoiler level") update-spoiler-level

  (action "pets <>") purr

  (command "alias") add-aliases
  (command "unalias") rm-aliases

  (raw "JOIN") on-join
  (raw "PART") on-quit
  (raw "QUIT") on-quit
  @state)


## License

Copyright © 2015 Ben 'ToxicFrog' Kelly

Released under the MIT license; see the LICENSE file for details.
