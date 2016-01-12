(ns lens.server
  (:use plumbing.core)
  (:require [com.stuartsierra.component :refer [Lifecycle]]
            [org.httpkit.server :refer [run-server]]
            [lens.app :refer [app]]
            [lens.util :as u]))

(defrecord Server [version port context-path thread token-store
                   authenticator stop-fn]
  Lifecycle
  (start [server]
    (let [handler (app {:version version
                        :context-path context-path
                        :token-store token-store
                        :authenticator authenticator})
          opts {:port port :thread thread}]
      (assoc server :stop-fn (run-server handler opts))))
  (stop [server]
    (stop-fn)
    (assoc server :stop-fn nil)))

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

(defnk new-server [{ip "0.0.0.0"} {port "80"} {context-path "/"} {thread "4"}]
  (map->Server {:ip ip
                :port (u/parse-long port)
                :context-path (parse-path context-path)
                :thread (u/parse-long thread)}))
