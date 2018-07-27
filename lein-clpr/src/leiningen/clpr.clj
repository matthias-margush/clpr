(ns leiningen.clpr
  (:require [leiningen.core.eval :refer [eval-in-project]]
            [leiningen.core.project :as project]))

(def clpr-profile
  {:dependencies '[[clpr "0.1.0-SNAPSHOT"]]})

(defn clpr
  "Starts a clpr."
  [project & args]
  (let [profile (or (:clpr (:profiles project)) clpr-profile)
        project (project/merge-profiles project [profile])]
    (eval-in-project project
                     `(clpr.repl/-main ~@args)
                     '(require 'clpr.repl))))

