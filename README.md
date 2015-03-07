# sharkybot2

A simple IRC bot for #gbchat, the Gentleman Bastards fan channel. Remembers pronouns and spoiler levels and eats people.

![kickin' rad shark portrait](https://raw.githubusercontent.com/ToxicFrog/sharkbot/master/sharky.png)

(Thanks to [Korbinnian Rittinger](http://korvidian.tumblr.com) for the drawing.)

## Installation

None needed, just download and run. State is stored in `sharky.edn` in PWD by default.

## Startup

    lein run -- [--server irc.freenode.net] [--port 6667] [--join #gbchat] [--nick SharkyMcJaws] [--persistence ./sharky.edn]

Supported options with their defaults are shown.

There is currently no support for joining multiple channels. You can do it by comma-separating the argument to `--join`, but the bot will misbehave badly if you do.

## Usage

The bot responds to various one-word commands, which can be delivered by !-prefixing them (`!eat Person`) or by prefixing them with the name of the bot (`SharkyMcJaws, eat Person`). It also responds to some emotes (/me commands) that are not specifically directed at it.

It supports the following commands:

    !teeth <person>
    !eat <person>
    /me feeds <person> to the shark
    /me sends <person> for teeth lessons

Send someone for teeth lessons.

    !set key value [key value ...]
    !unset key [key ...]

Set or clear user-specific info, as in `!set name Ben pronouns m spoilers RoT country Canada tz EST`, or `!unset name`.

    !info <person>
    SharkyMcJaws, tell me about <person>

Display info someone previously recorded with `!set`.

    !alias name [name ...]
    !unalias name [name ...]

Record aliases. When asked about these aliases the bot will respond as though asked about you, assuming that they are unambiguous. If multiple people use the same alias, it will ask for clarification.

    !remember key text...
    !remember key
    !forget key

Remember or forget `text` as associated with `key`. Without `text`, display the previously remembered text.

    !spoilers
    SharkyMcJaws, check the spoiler level
    SharkyMcJaws, what's the spoiler level?

Check the spoiler level. This is normally handled automatically based on what users have set as their `spoiler` info (it recognizes `LoLL` for Lies, `RSURS` for Seas, and `RoT` for Republic), but if you want to double check you can use this command.

    !newbie <person>
    SharkyMcJaws, there's a newbie
    SharkyMcJaws, <person> is a newbie

Provides a link to the newbie guide, optionally directed at a specific person.


## Roadmap

- proper logging
- book synonyms and case insensitivity
- random text for !eat, see suggestions
  <Ariaste> "Sharky nibbles (thing) inquisitively, then spits it out. He gives you a disgusted look and swims away."
  <booty> Sharky eats [instert thing] and gets indigestion. You gave sharky tummy. You should feel bad about your life and choices
  <Ariaste> Sharky chomps (thing) enthusiastically.
  <booty> Sharky plays with his food. Good sharky.
  <semirose> SharkyMcJaws swims alongside brother/sister of the lady of the long silence
- respond to "sharky" as well as "SharkyMcJaws"
- multichannel support

## License

Copyright © 2015 Ben 'ToxicFrog' Kelly
SharkyMcJaws drawing © 2015 Korbinnian Rittinger

Released under the MIT license; see the LICENSE file for details.
