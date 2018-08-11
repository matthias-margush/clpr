(ns clpr.tools
  "clpr tools"
  (:require [cljfmt.main :as cljfmt]
            [clojure.repl :as repl]
            [clojure.string :as str]
            [clojure.test :refer [*test-out*]]
            [clojure.tools.namespace.file :as ns-file]
            [clojure.tools.namespace.repl :as ns]
            [clpr.repl :refer [*clpr-out*]]
            [clpr.tools.test-report :as clpr-report]
            [compliment.core :as cc]
            [eastwood.lint :as lint]
            [eftest.report :refer [report-to-file]]
            [eftest.report.junit]
            [eftest.runner :as ef]))

(defn refresh
  "Refreshes namespaces that have changed and sets the current name space to the current file's."
  [f]
  (ns/refresh)
  (eval (ns-file/read-file-ns-decl f)))

(defn refresh-all
  "Refreshes all namespaces and sets the current name space to the current file's."
  [f]
  (ns/refresh-all)
  (eval (ns-file/read-file-ns-decl f)))

(defn run-tests
  "Run all tests."
  []
  (doseq [test-dir (:test-paths clpr.repl/project)]
    (binding [*test-out* *clpr-out*
              clpr-report/*test-dir* test-dir]
      (ef/run-tests (ef/find-tests test-dir)
                    {:report clpr-report/report
                     :capture-output? false
                     :multithread? false}))))

(defn fmt
  "Formats the the file."
  [f]
  (cljfmt/-main "fix" f))

(defn lint
  "Lint the project."
  []
  (binding [*out* *clpr-out*]
    (lint/eastwood {:source-paths ["src" "test"]})))

(defmacro doc
  "Wraps clojure.repl/doc."
  [s]
  `(binding [*out* *clpr-out*]
     (repl/doc ~s)))

(defmacro source
  "Wraps clojure.repl/source."
  [s]
  `(binding [*out* *clpr-out*]
     (repl/source ~s)))

(defn completions
  "Return possible completions of text `t` in `context`."
  [t ctx]
  (binding [*out* *clpr-out*]
    (doseq [completion (cc/completions t {:ns *ns* :plain-candidates true})]
      (println completion))))

