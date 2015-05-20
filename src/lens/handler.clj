(ns lens.handler
  (:use plumbing.core)
  (:require [liberator.core :refer [resource]]
            [liberator.representation :refer [as-response]])
  (:import [java.util UUID]))

(def resource-defaults
  {:available-media-types ["application/json"]
   :allowed-methods [:post]

   :new? false
   :respond-with-entity? true

   :handle-unprocessable-entity (fnk [error] {:error error})})

(defn token-handler [db]
  (resource
    resource-defaults

    :processable?
    (fnk [[:request params]]
      (cond
        (or (nil? (:grant_type params))
            (nil? (:username params))
            (nil? (:password params)))
        [false {:error "invalid_request"}]

        (not= "password" (:grant_type params))
        [false {:error "unsupported_grant_type"}]

        :else true))

    :post!
    (fnk [[:request [:params username]]]
      (let [token (str (UUID/randomUUID))]
        (swap! db #(assoc % token {:username username}))
        {:token token}))

    :as-response
    (fn [d ctx]
      (-> (as-response d ctx)
          (assoc-in [:headers "cache-control"] "no-store")
          (assoc-in [:headers "pragma"] "no-cache")))

    :handle-ok
    (fnk [token]
      {:access_token token
       :token_type "bearer"})))

(defn introspect-handler [db]
  (resource
    resource-defaults

    :processable?
    (fnk [[:request params]]
      (cond
        (nil? (:token params))
        [false {:error "invalid_request"}]

        :else true))

    :post!
    (fnk [[:request [:params token]]]
      (if-let [user-info (get @db token)]
        {::resp
         {:active true
          :username (:username user-info)}}
        {::resp
         {:active false}}))

    :handle-ok ::resp))

(defn handlers [db]
  {:token (token-handler db)
   :introspect (introspect-handler db)})
