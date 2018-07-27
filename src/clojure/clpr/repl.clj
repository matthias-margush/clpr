(ns clpr.repl
  (:gen-class)
  (:require [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [clpr Client]
           [java.io BufferedReader BufferedWriter StringReader StringWriter]
           [java.net ServerSocket Socket]))

(defn run-repl
  ""
  [{::keys [port server] :as repl-server}]
  (binding [*ns* (create-ns 'user)
            *e nil
            *1 nil
            *2 nil
            *3 nil]
    (while true
      (with-open [socket (.accept server)]
        (try
          (with-open [in (io/reader (-> socket
                                       (.getInputStream)))
                      out (io/writer (.getOutputStream socket))]
            (let [buf (StringWriter.)
                  code (Client/unwrap in (BufferedWriter. buf))
                  lines (str "(do " (.toString buf) ")")]
              (try
                (let [result (str (eval (read-string lines)))]
                  (Client/wrap (BufferedReader. (StringReader. result)) out))
                (catch Throwable e
                  (.printStackTrace e)
                  (Client/wrap (BufferedReader. (StringReader. (str e))) out)))
              (.read in)))
          (catch Throwable e
            (.printStackTrace e)))))))

(defn start
  ""
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
  ""
  [repl]
  (swap! repl
         (fn [{::keys [server] :as repl}]
           (when server
             (println "Stopping server")
             (.close server))
           (dissoc repl ::server))))

(defn restart
  ""
  [repl]
  (stop repl)
  (start repl))

(defn repl
  [host port]
  (atom {::host host ::port port}))

(defn -main
  ""
  [& [host port]]
  (let [host (or host "localhost")
        port (or port "0")
        repl (repl host (Integer/parseInt port))]
    (start repl)))

