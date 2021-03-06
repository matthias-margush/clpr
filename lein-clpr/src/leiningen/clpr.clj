(ns leiningen.clpr
  (:require [leiningen.core.eval :refer [eval-in-project]]
            [leiningen.core.main :refer [info]]
            [leiningen.core.project :as project]))

(def clpr-profile
  {:dependencies '[[clpr "0.1.4"]]})

(defn clpr
  "Starts a clpr."
  [project & args]
  (let [profile (or (:clpr (:profiles project)) clpr-profile)
        project (project/merge-profiles project [profile])
        init-options `(do ~(:init (:clpr project))
                           (require 'clpr.repl)
                           (clpr.repl/set-project! '~project))]

    (eval-in-project project
                     `(clpr.repl/-main ~@args)
                     init-options)))

