(ns clpr.repl
  "Simple repl server"
  (:gen-class)
  (:require [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.io BufferedWriter PrintWriter StringWriter]
           [java.net ServerSocket]
           [java.util UUID]))

(def ^:dynamic *clpr-out*)

(defn- p
  "Pretty print a form."
  [form]
  (str/trim
   (with-out-str (pprint form))))

(defn- try-eval
  "Returns the result of read/eval on lines, or the exception."
  [repl lines out]
  (binding [*clpr-out* out
            *ns* (create-ns 'user)
            *e @(::*e repl)
            *1 @(::*1 repl)
            *2 @(::*2 repl)
            *3 @(::*3 repl)]
    (try
      (let [result (eval (read-string lines))]
        (reset! (::*3 repl) @(::*2 repl))
        (reset! (::*2 repl) @(::*1 repl))
        (reset! (::*1 repl) result)
        (p result))
      (catch Throwable e
        (let [ep (p (Throwable->map e))]
          (reset! (::*e repl) ep)
          ep)))))

(defn- unwrap
  "Unwrap a client message"
  [in]
  (with-open [buf (StringWriter.)
              out (PrintWriter. buf)]
    (let [marker (.readLine in)]
      (loop [line (.readLine in)]
        (when-not (= line marker)
          (.println out line)
          (recur (.readLine in)))))
    (str "(do" (.toString buf) ")")))

(defn- run-repl
  "Runs the repl server."
  [{::keys [port server] :as repl-server}]
  (try
    (while true
      (with-open [socket (.accept server)]
        (try
          (with-open [in  (io/reader (.getInputStream socket))
                      out (PrintWriter. (io/writer (.getOutputStream socket)))]
            (let [marker (str (UUID/randomUUID))
                  code   (unwrap in)]
              (.println out marker)
              (.println out (try-eval repl-server code out))
              (.println out marker)
              (.flush out)
              (.read in)))
          (catch Throwable e
            (.printStackTrace e)))))
    (catch Throwable e
      (println "Disconnected"))))

(defn start
  "Starts the repl server if not running, updating the atom `repl`."
  [repl]
  (swap! repl
         (fn [{::keys [host port server] :as repl}]
           (if server
             repl
             (do
               (let [server (ServerSocket. port)
                     repl (assoc repl ::server server)
                     port (.getLocalPort server)]
                 (printf "clpr server started on port %s on host %s\n" port host)
                 (spit ".clpr-port" (.getLocalPort server))
                 (future (run-repl repl))
                 repl))))))

(defn stop
  "Stops the repl server if not running, updating the atom `repl`."
  [repl]
  (swap! repl
         (fn [{::keys [server] :as repl}]
           (when server
             (println "Stopping server")
             (.close server))
           (dissoc repl ::server))))

(defn restart
  "Restarts the repl server if not running, updating the atom `repl`."
  [repl]
  (stop repl)
  (start repl))

(defn repl
  "Creates a repl server that will listen on `host` and `port`."
  [host port]
  (atom {::host host
         ::port port
         ::*1 (atom nil)
         ::*2 (atom nil)
         ::*3 (atom nil)
         ::*e (atom nil)}))

(defn -main
  "Runs the repl server from the command line."
  [& [host port]]
  (let [host (or host "localhost")
        port (or port "0")
        repl (repl host (Integer/parseInt port))]
    (start repl)))
