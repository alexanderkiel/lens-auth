(ns lens.core
  (:use plumbing.core)
  (:require [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [org.httpkit.server :refer [run-server]]
            [lens.app :refer [app]]
            [lens.util :refer [parse-int]]))

(defn- ensure-facing-separator [path]
  (if (.startsWith path "/")
    path
    (str "/" path)))

(defn- remove-trailing-separator [path]
  (if (.endsWith path "/")
    (subs path 0 (dec (count path)))
    path))

(defn- parse-path [path]
  (if (= "/" path)
    path
    (-> path ensure-facing-separator remove-trailing-separator)))

(def cli-options
  [["-p" "--port PORT" "Listen on this port"
    :default 8080
    :parse-fn parse-int
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-i" "--ip" "The IP to bind"
    :default "0.0.0.0"]
   ["-t" "--thread" "Number of worker threads"
    :default 4
    :parse-fn parse-int
    :validate [#(< 0 % 64) "Must be a number between 0 and 64"]]
   ["-c" "--context-path PATH"
    "An optional context path under which the workbook service runs."
    :default "/"
    :parse-fn parse-path]
   ["-h" "--help" "Show this help"]])

(defn usage [options-summary]
  (->> ["Usage: lens-auth [options]"
        ""
        "Options:"
        options-summary
        ""]
       (str/join "\n")))

(defn error-msg [errors]
  (str/join "\n" errors))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [options errors summary]} (cli/parse-opts args cli-options)
        version (System/getProperty "lens-auth.version")]
    (cond
      (:help options)
      (exit 0 (usage summary))

      errors
      (exit 1 (error-msg errors)))

    (letk [[context-path] options]
      (run-server (app (assoc options :db (atom {}) :version version))
                  (merge {:worker-name-prefix "http-kit-worker-"} options))
      (println "Version:" version)
      (println "Max Memory:" (quot (.maxMemory (Runtime/getRuntime))
                                   (* 1024 1024)) "MB")
      (println "Num CPUs:" (.availableProcessors (Runtime/getRuntime)))
      (println "Context Path:" context-path)
      (println "Server started")
      (println "Listen at" (str (:ip options) ":" (:port options)))
      (println "Using" (:thread options) "worker threads"))))
