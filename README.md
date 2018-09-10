# clpr

clpr - (CL)ojure (PR)inter
clpr - (C)lojure (L)ine (P)(R)inter
clpr - (C)ommand (L)ine (P)(R)inter

[![Clojars Project](https://img.shields.io/clojars/v/clpr.svg)](https://clojars.org/clpr)

## Usage
clpr is a simple command line tool that ferries its input to a remote clojure
repl, and outputs the result. I wrote it to make working with clojure in vim
simpler, although it isn't a vim plugin, and could be used with any editor that
can work with shell commands.

```
჻ echo '(+ 2 3)' | clpr
5
```

It makes possible quick interactions with a repl from the command line.  This
allows the clojure repl to be integrated with the many ways that vim can work
with external shell commands.

```
nnoremap K :!echo '(with-out-str (clojure.repl/doc <c-r><c-w>))' \| clpr<cr>
```

## Installation

To install clpr client command, put [clpr](clpr) on your path. By default,
`clpr` will connect to localhost and the port in `.clpr-port` in the directory
where it's run.

The simplest way to run the server is:
```
lein clpr
```

After adding this to ~/.lein/profiles.clj:
```
{:user {:dependencies [[clpr-tools "0.1.4"]]
        :plugins [[lein-clpr "0.1.4"]]
        :clpr {:init (require 'clpr.tools)}}}
```

[clpr-tools](clpr-tools) provides some convenience wrappers that are useful for keybindings.

I've started experimenting with some vim keybindings for this [here](https://github.com/matthias-margush/dot/blob/master/.config/nvim/clojure.vim).

## License

Copyright © 2018 Matthias Margush

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
