(ns lens.app
  (:require [lens.route :refer [routes]]
            [lens.handler :refer [handlers]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [lens.middleware.cors :refer [wrap-cors]]
            [bidi.ring :as bidi-ring]
            [io.clojure.liberator-transit]))

(defn app [db]
  (-> (bidi-ring/make-handler routes (handlers db))
      (wrap-cors)
      (wrap-keyword-params)
      (wrap-params)))
