(ns lens.app
  (:use plumbing.core)
  (:require [bidi.bidi :as bidi]
            [bidi.ring :as bidi-ring]
            [lens.route :refer [routes]]
            [lens.handler :refer [handlers]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [lens.middleware.cors :refer [wrap-cors]]))

(defn path-for [routes]
  (fn [handler & params]
    (apply bidi/path-for routes handler params)))

(defn wrap-path-for [handler path-for]
  (fn [req] (handler (assoc req :path-for path-for))))

(defn wrap-not-found [handler]
  (fn [req]
    (if-let [resp (handler req)]
      resp
      {:status 404})))

(defnk app [context-path :as ctx]
  (assert (re-matches #"/(?:.*[^/])?" context-path))
  (let [routes (routes context-path)
        path-for (path-for routes)]
    (-> (bidi-ring/make-handler routes (handlers ctx))
        (wrap-path-for path-for)
        (wrap-not-found)
        (wrap-cors)
        (wrap-keyword-params)
        (wrap-params))))
