(defproject clpr "0.1.2"
  :description "(or \"(CL)ojure (PR)inter\" \"(C)lojure (L)ine (PR)inter\" \"(C)command (L)ine (PR)inter\")"
  :url "https://github.com/matthias-margush/clpr"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main clpr.repl
  :aot [clpr.repl]
  :dependencies  [[org.clojure/clojure "1.9.0" :scope "provided"]])
