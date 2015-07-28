# sharkbot

A simple IRC bot for #gbchat, the Gentleman Bastards fan channel. Remembers pronouns and spoiler levels and eats people.

![kickin' rad shark portrait](https://raw.githubusercontent.com/ToxicFrog/sharkbot/master/sharky.png)

(Thanks to [Korbinnian Rittinger](http://korvidian.tumblr.com) for the drawing.)

## Installation

None needed, just download and run. State is stored in `sharky.edn` in PWD by default.

## Startup

    lein run --
        [--server irc.freenode.net]
        [--port 6667]
        [--join #gbchat]
        [--nick SharkyMcJaws,sharky]
        [--persistence ./sharky.edn]
        [--admin Alice,Bob]
        [--modules amusements,userinfo,spoilers,memory]

Supported options with example values are shown. There is also a `--help` option that displays a brief help text.

There is currently no support for joining multiple channels. You can do it by comma-separating the argument to `--join`, but the bot will misbehave badly if you do.

    --server SERVER
    --port PORT
    --join CHANNEL

Specifies the server and port to connect to and the channel to join.

    --nick FOO,BAR,...

Comma-separated list of names. The first one will be used as the bot's IRC name (and it will error if it can't claim that name). The other names are alternate names it will respond to when addressed using them in channel.

    --persistence FILE

Path to the persistence file to store user info and memories in.

    --admin NAME,NAME,...

Comma-separated list of names to act as administrators. At the moment all this does is give those people the ability to use the hot-reload command to reload specific modules. It may do more in the future.

    --modules MODULE,MODULE,...

List of modules (from `src/sharkbot/modules`) to load at startup, or when told to hot-reload by a non-admin user. The default is to load all modules.

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

Set or clear user-specific info, as in `!set name Ben pronouns m spoilers RoT country Canada tz EST`, or `!unset name`. This can't be used to set aliases; see `!alias` and `!unalias` for that.

The bot will understand `pronouns` (which should be `t`, `f`, or `m`), `name`, and `spoilers`; other fields are recorded purely for informational purposes.

    !info <person>
    SharkyMcJaws, tell me about <person>

Display info someone previously recorded with `!set`.

    !alias name [name ...]
    !unalias name [name ...]

Record aliases. When asked about these aliases the bot will respond as though asked about you, assuming that they are unambiguous. If multiple people use the same alias, it will ask for clarification.

    !remember key text...
    !remember key
    !remember
    !forget key

Remember or forget `text` as associated with `key`. Without `text`, display the previously remembered text. With no `key`, list the keys of all the bot's memories.

    !spoilers
    SharkyMcJaws, check the spoiler level
    SharkyMcJaws, what's the spoiler level?

Check the spoiler level. This is normally handled automatically based on what users have set as their `spoiler` info (it recognizes `LoLL` for Lies, `RSURS` for Seas, and `RoT` for Republic), but if you want to double check you can use this command.

    !newbie <person>
    SharkyMcJaws, there's a newbie
    SharkyMcJaws, <person> is a newbie

Provides a link to the newbie guide, optionally directed at a specific person.

    !hot-reload
    !hot-reload <modules>

Reload modules without restarting the bot.

When used without arguments, or by non-admins, reload all feature modules specified on the command line. When passed arguments by an admin, reload exactly those modules; these should be Clojure module paths without the leading `sharkbot.` prefix, so `!hot-reload triggers modules.memory` to reload the main triggers library and the memory feature module.

Note for admins: reloading `triggers` will reset the trigger table, disabling all features. It's recommended to do an unqualified `hot-reload` afterwards to reload the feature modules as well.

## Roadmap

- proper logging
- random text for !eat, see suggestions
  <Ariaste> "Sharky nibbles (thing) inquisitively, then spits it out. He gives you a disgusted look and swims away."
  <booty> Sharky eats [instert thing] and gets indigestion. You gave sharky tummy. You should feel bad about your life and choices
  <Ariaste> Sharky chomps (thing) enthusiastically.
  <booty> Sharky plays with his food. Good sharky.
  <semirose> SharkyMcJaws swims alongside brother/sister of the lady of the long silence
- multichannel support
- support regexes in (command) triggers for e.g. (command #"spoilers\?*")

## License

Copyright © 2015 Ben 'ToxicFrog' Kelly
SharkyMcJaws drawing © 2015 Korbinnian Rittinger

Released under the MIT license; see the LICENSE file for details.
