(ns clpr.tools.test-report
  "Pretty test reporter with full paths."
  (:require [clojure.test :as test]
            [clojure.data :as data]
            [clojure.stacktrace :as st]
            [io.aviso.ansi :as ansi]
            [io.aviso.exception :as exception]
            [io.aviso.repl :as repl]
            [puget.printer :as puget]
            [fipp.engine :as fipp]
            [eftest.output-capture :as capture]
            [eftest.report :as report]
            [clojure.string :as str]))

(def ^:dynamic *test-dir*
  "The test directory."
  "test")

(def ^:dynamic *divider*
  "The divider to use between test failure and error reports."
  "")

(defn- test-dir
  "Gets the test directory."
  []
  (let [cwd (str (System/getProperty "user.dir") "/")]
    (if (str/starts-with? *test-dir* cwd)
      (subs *test-dir* (count cwd))
      *test-dir*)))

(defn- get-line
  ""
  [actual file line]
  (if (instance? Throwable actual)
    (let [file (last (str/split file #"/"))]
      (->> (:trace (Throwable->map actual))
           (keep (fn [[_ _ f n]] (if (= file f) n)))
           (first)))))

(defn- testing-scope-str [{:keys [actual file line] :as m}]
  (let [[ns scope] report/*testing-path*
        file (or (:file (meta scope)) file)]
    (str
     (cond
       (keyword? scope)
       (str (ns-name ns) " during " scope)

       (var? scope)
       (str (ns-name ns) "/" (:name (meta scope))))
     (let [line (or (get-line actual file line) line)]
       (when (and file line)
         (str " (" (test-dir) "/" file ":" line ")"))))))

(defn- diff-all [expected actuals]
  (map vector actuals (map #(take 2 (data/diff expected %)) actuals)))

(defn- pretty-printer []
  (puget/pretty-printer {:print-color false
                         :print-meta false}))

(defn- pprint-document [doc]
  (fipp/pprint-document doc {:width 80}))

(defn- equals-fail-report [{:keys [actual]}]
  (let [[_ [_ expected & actuals]] actual
        p (pretty-printer)]
    (doseq [[actual [a b]] (diff-all expected actuals)]
      (pprint-document
        [:group
         [:span "expected: " (puget/format-doc p expected) :break]
         [:span "  actual: " (puget/format-doc p actual) :break]
         (when (and (not= expected a) (not= actual b))
           [:span "    diff: "
            (if a
              [:span "- " (puget/format-doc p a) :break])
            (if b
              [:span
               (if a  "          + " "+ ")
               (puget/format-doc p b)])])]))))

(defn- predicate-fail-report [{:keys [expected actual]}]
  (let [p (pretty-printer)]
    (pprint-document
      [:group
       [:span "expected: " (puget/format-doc p expected) :break]
       [:span "  actual: " (puget/format-doc p actual)]])))

(defn- print-stacktrace [t]
  (binding [exception/*fonts* nil]
    (repl/pretty-print-stack-trace t test/*stack-trace-depth*)))

(defn- error-report [{:keys [expected actual]}]
  (if expected
    (let [p (pretty-printer)]
      (pprint-document
       [:group
        [:span "expected: " (puget/format-doc p expected) :break]
        [:span "  actual: " (with-out-str (print-stacktrace actual))]]))
    (print-stacktrace actual)))

(defn- print-output [output]
  (when-not (str/blank? output)
    (println "--- Test output ---")
    (println (str/trim-newline output))
    (println "-------------------")))

(defmulti report
  "A reporting function compatible with clojure.test. Uses ANSI colors and
  terminal formatting to produce readable and 'pretty' reports."
  :type)

(defmethod report :default [m])

(defmethod report :pass [m]
  (test/with-test-out (test/inc-report-counter :pass)))

(defmethod report :fail [{:keys [message expected] :as m}]
  (test/with-test-out
    (test/inc-report-counter :fail)
    (print *divider*)
    (println (str "FAIL in") (testing-scope-str m))
    (when (seq test/*testing-contexts*) (println (test/testing-contexts-str)))
    (when message (println message))
    (if (and (sequential? expected)
             (= (first expected) '=))
      (equals-fail-report m)
      (predicate-fail-report m))
    (print-output (capture/read-test-buffer))))

(defmethod report :error [{:keys [message expected actual] :as m}]
  (test/with-test-out
    (test/inc-report-counter :error)
    (print *divider*)
    (println (str  "ERROR in") (testing-scope-str m))
    (when (seq test/*testing-contexts*) (println (test/testing-contexts-str)))
    (when message (println message))
    (error-report m)
    (some-> (capture/read-test-buffer) (print-output))))

(defn- pluralize [word count]
  (if (= count 1) word (str word "s")))

(defn- format-interval [duration]
  (format "%.3f seconds" (double (/ duration 1e3))))

(defmethod report :long-test [{:keys [duration] :as m}]
  (test/with-test-out
    (print *divider*)
    (println (str "LONG TEST in") (testing-scope-str m))
    (when duration (println "Test took" (format-interval duration) "seconds to run"))))

(defmethod report :summary [{:keys [test pass fail error duration]}]
  (let [total (+ pass fail error)]
    (test/with-test-out
      (print *divider*)
      (println "Ran" test "tests in" (format-interval duration))
      (println (str
                    total " " (pluralize "assertion" total) ", "
                    fail  " " (pluralize "failure" fail) ", "
                    error " " (pluralize "error" error) ".")))))
