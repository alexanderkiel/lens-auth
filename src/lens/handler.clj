(ns lens.handler
  (:use plumbing.core)
  (:require [liberator.core :refer [resource]]
            [liberator.representation :refer [as-response]]
            [lens.store :as store])
  (:import [java.util UUID]))

(def resource-defaults
  {:available-media-types ["application/json"]
   :allowed-methods [:post]

   :new? false
   :respond-with-entity? true

   :handle-unprocessable-entity (fnk [error] {:error error})})

(defn token-handler [token-store]
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
        (store/put! token-store token {:username username})
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

(defn introspect-handler [token-store]
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
      (if-letk [[username] (store/get token-store token)]
        {::resp
         {:active true
          :username username}}
        {::resp
         {:active false}}))

    :handle-ok ::resp))

(defn handlers [token-store]
  {:token (token-handler token-store)
   :introspect (introspect-handler token-store)})
