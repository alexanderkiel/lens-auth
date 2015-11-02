(ns lens.system
  (:use plumbing.core)
  (:require [org.httpkit.server :refer [run-server]]
            [lens.util :refer [parse-long]]
            [lens.app :refer [app]]
            [lens.store.atom :refer [create-atom]]
            [com.stuartsierra.component :as component]))

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

(defn- create-token-store [{:keys [token-store]}]
  (case (some-> token-store .toLowerCase)
    (create-atom)))

(defn create [env]
  (-> (assoc env :app app)
      (assoc :version (:lens-auth-version env))
      (assoc :token-store (create-token-store env))
      (update :context-path (fnil parse-path "/"))
      (update :ip (fnil identity "0.0.0.0"))
      (update :port (fnil parse-long "80"))
      (update :thread (fnil parse-long "4"))))

(defnk start [app port & more :as system]
  (assert (nil? (:stop-fn system)) "System already started.")
  (let [more (update more :token-store component/start)
        stop-fn (run-server (app more) {:port port})]
    (assoc system :stop-fn stop-fn)))

(defn stop [{:keys [stop-fn] :as system}]
  (stop-fn)
  (dissoc system :stop-fn))
