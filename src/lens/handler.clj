(ns lens.handler
  (:use plumbing.core)
  (:require [liberator.core :refer [resource]]
            [liberator.representation :refer [as-response ring-response]]
            [lens.store :as store]
            [lens.auth :as auth])
  (:import [java.util UUID]))

(def resource-defaults
  {:available-media-types ["application/json"]
   :allowed-methods [:post]

   :new? false
   :respond-with-entity? true

   :handle-unprocessable-entity
   (fnk [error]
     ;Workaround for https://github.com/clojure-liberator/liberator/issues/94
     (ring-response {:error error} {:status 400}))})

(defnk token-handler [token-store authenticator]
  (resource
    resource-defaults

    :processable?
    (fnk [[:request params]]
      (let [{:keys [grant_type username password]} params]
        (cond
          (not (and grant_type username password))
          [false {:error "invalid_request"}]

          (not= "password" grant_type)
          [false {:error "unsupported_grant_type"}]

          (not (auth/check-credentials authenticator username password))
          [false {:error "invalid_grant"}]

          :else true)))

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

(defnk introspect-handler [token-store]
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

(defn handlers [ctx]
  {:token (token-handler ctx)
   :introspect (introspect-handler ctx)})
