(ns lens.app
  (:use plumbing.core)
  (:require [lens.route :refer [routes]]
            [lens.handler :refer [handlers]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [lens.middleware.cors :refer [wrap-cors]]
            [bidi.ring :as bidi-ring]))

(defn wrap-not-found [handler]
  (fn [req]
    (if-let [resp (handler req)]
      resp
      {:status 404})))

(defnk app [db context-path]
  (assert (re-matches #"/(?:.*[^/])?" context-path))
  (let [routes (routes context-path)]
    (-> (bidi-ring/make-handler routes (handlers db))
        (wrap-not-found)
        (wrap-cors)
        (wrap-keyword-params)
        (wrap-params))))
