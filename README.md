# HipChat / Sensu Addon

This project aims to provide integration of [Sensu] monitoring with [HipChat] chat client.

At this point it isn't even alpha and hasn't been tested against the atlassian
servers. It also has no tests. And this is my first clojure project so it is
bound to be messy.

[Sensu]: https://sensuapp.org/
[HipChat]: http://hipchat.com/

## TODO

- Persistent storage of installations (try enduro?)
- Glance click action
- Sidebar view
- Tests!

## Dev Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2016 Apache License 2.0
