(defproject clpr "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main clpr.repl
  :aot [clpr.repl]
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :dependencies  [[org.clojure/clojure "1.9.0"]])
