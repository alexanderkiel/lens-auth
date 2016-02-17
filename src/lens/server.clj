(ns lens.server
  (:use plumbing.core)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [schema.core :as s :refer [Str]]
            [com.stuartsierra.component :refer [Lifecycle]]
            [org.httpkit.server :refer [run-server]]
            [lens.app :refer [app]]
            [lens.util :as u]))

(s/defn ^:private read-i18n-resource [i18n-name :- Str]
  (edn/read-string (slurp (io/resource (str "i18n-" i18n-name ".edn")))))

(defrecord Server [version port context-path thread token-store client-store
                   authenticator i18n-name stop-fn]
  Lifecycle
  (start [server]
    (let [handler (app {:version version
                        :context-path context-path
                        :token-store token-store
                        :client-store client-store
                        :authenticator authenticator
                        :i18n (read-i18n-resource i18n-name)})
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
