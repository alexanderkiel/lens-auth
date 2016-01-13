(ns lens.core
  (:use plumbing.core)
  (:require [com.stuartsierra.component :as comp]
            [environ.core :refer [env]]
            [lens.system :as system]
            [lens.descriptive :refer [describe]]))

(defn- max-memory []
  (quot (.maxMemory (Runtime/getRuntime)) (* 1024 1024)))

(defn- available-processors []
  (.availableProcessors (Runtime/getRuntime)))

(defn -main [& _]
  (letk [[version [:server ip port context-path thread] token-store client-store
          authenticator :as system] (system/new-system env)]
    (comp/start system)
    (println "Version:" version)
    (println "Max Memory:" (max-memory) "MB")
    (println "Num CPUs:" (available-processors))
    (println "Context Path:" context-path)
    (println "Token store:" (describe token-store))
    (println "Generated tokens will expire after" (:expire token-store) "ms")
    (println "Client store:" (describe client-store))
    (println "Authenticator:" (describe authenticator))
    (println "Server started")
    (println "Listen at" (str ip ":" port))
    (println "Using" thread "worker threads")))
