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
{:user {:dependencies [[clpr-tools "0.1.2"]]
        :plugins [[lein-clpr "0.1.2"]]
        :clpr {:init (require 'clpr.tools)}}}
```

[clpr-tools](clpr-tools) provides some convenience wrappers that are useful for keybindings.

Here are some ideas for vim keybindings:
```
" Start, stop and switch to clpr:
autocmd FileType clojure nnoremap <silent> <localleader>si :terminal lein clpr<cr>:keepalt :file *clpr*<cr>:b#<cr>
autocmd FileType clojure nnoremap <silent> <localleader>sq :bd! \*clpr\*<cr>
autocmd FileType clojure nnoremap <silent> <localleader>ss :sb \*clpr\*<cr>

" Refresh namespaces
autocmd FileType clojure nnoremap <silent> <localleader>dr :AsyncRun echo '(clpr.tools/refresh "%:p")' \| clpr<cr>
autocmd FileType clojure nnoremap <silent> <localleader>dR :AsyncRun echo '(clpr.tools/refresh-all "%:p")' \| clpr<cr>

" Run tests
autocmd FileType clojure nnoremap <silent> <localleader>tp :AsyncRun echo '(clpr.tools/run-tests)' \| clpr<cr>:copen<cr>

" Show docs, source
autocmd FileType clojure nnoremap <silent>K :!echo '(clpr.tools/doc <c-r><c-w>)' \| clpr<cr>
autocmd FileType clojure nnoremap <silent> <localleader>s :!echo '(clpr.tools/source <c-r><c-w>)' \| clpr<cr>

" Format the current buffer
autocmd FileType clojure nnoremap <silent> <localleader>fb :w<cr>:!echo '(clpr.tools/fmt "%:p")' \| clpr<cr>:e %<cr>

" Lint the project:
nnoremap <localleader>pl :wa \| AsyncRun echo '(clpr.tools/lint)' \| clpr<cr>:copen<cr>

" Eval movements and selections:
autocmd FileType clojure nnoremap <silent> <localleader>ae :set opfunc=AsyncClojureEval<cr>g@
autocmd FileType clojure vnoremap <silent> <localleader>ae :AsyncRun clpr<cr>
autocmd FileType clojure nnoremap <silent> <localleader>e :set opfunc=ClojureEval<cr>g@
autocmd FileType clojure vnoremap <silent> <localleader>e :w !clpr<cr>

function! ClojureEval(type, ...)
    '[,']w !clpr
endfunction

function! AsyncClojureEval(type, ...)
    silent exe "'[,']AsyncRun clpr"
    silent exe "copen"
endfunction
```

## License

Copyright © 2018 Matthias Margush

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
