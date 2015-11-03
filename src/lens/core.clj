(ns lens.core
  (:use plumbing.core)
  (:require [environ.core :refer [env]]
            [lens.system :as system]))

(defn- max-memory []
  (quot (.maxMemory (Runtime/getRuntime)) (* 1024 1024)))

(defn- available-processors []
  (.availableProcessors (Runtime/getRuntime)))

(defn -main [& args]
  (letk [[ip port thread version context-path :as system] (system/create env)]
    (system/start system)
    (println "Version:" version)
    (println "Max Memory:" (max-memory) "MB")
    (println "Num CPUs:" (available-processors))
    (println "Context Path:" context-path)
    (println "Server started")
    (println "Listen at" (str ip ":" port))
    (println "Using" thread "worker threads")))
