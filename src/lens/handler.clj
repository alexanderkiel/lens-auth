(ns lens.handler
  (:use plumbing.core)
  (:require [liberator.core :refer [resource]]
            [liberator.representation :refer [as-response ring-response]]
            [lens.token-store :as ts]
            [lens.client-store :as cs]
            [lens.auth :as auth]
            [hiccup.core :refer [html]])
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

(defnk token-handler
  "Implements the token endpoint as described in RFC 6749 Section 4.3.2.

  The following request parameters are supported:

    grant_type - Value MUST be set to \"password\".
    username   - The resource owner username.
    password   - The resource owner password.

  Client authentication is not supported.

  Returns a JSON response containing a map with the following keys:

    access_token - The access token issued by the authorization server.
    token_type   - Currently always \"bearer\""
  [token-store authenticator]
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

          :else
          (if-let [user-info (auth/check-credentials authenticator username password)]
            {:user-info user-info}
            [false {:error "invalid_grant"}]))))

    :post!
    (fnk [[:user-info username]]
      (let [token (str (UUID/randomUUID))]
        (ts/put-token! token-store token {:username username})
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

(defn location [redirect-error request]
  (letk [[[:params redirect_uri {state nil}]] request]
    (cond-> (str redirect_uri "#error=" redirect-error)
      state (str "&state=" state))))

(defnk authorization-handler
  "Implements the authorization endpoint as described in RFC 6749 Section 4.2.1.

  The following request parameters are supported:

    response_type - Value MUST be set to \"token\".
    client_id     - The client identifier as described in Section 2.2.
    redirect_uri  - As described in Section 3.1.2.
    state         - An opaque value used by the client to maintain state between
                    the request and callback. (optional)"
  [client-store]
  (resource
    :available-media-types ["text/html"]
    :allowed-methods [:get]

    :processable?
    (fnk [[:request params]]
      (let [{:keys [response_type client_id redirect_uri]} params]
        (if-letk [[redirect-uris] (cs/get-client client-store client_id)]
          (if (some #(= redirect_uri %) redirect-uris)
            (if (= "token" response_type)
              true
              [false {:redirect-error "unsupported_response_type"}])
            [false {:user-error (str "Invalid redirection URI.")}])
          [false {:user-error "Invalid client identifier."}])))

    :handle-ok
    (fnk [[:request [:params client_id redirect_uri {state nil}]]]
      (html
        [:html
         [:head [:title "Sign In"]]
         [:body
          [:h1 "Sign In"]
          [:form {:method "POST" :action (str "sign-in")}
           [:input {:type "hidden" :name "client_id" :value client_id}]
           [:input {:type "hidden" :name "redirect_uri" :value redirect_uri}]
           (when state [:input {:type "hidden" :name "state" :value state}])
           [:input {:type "text" :name "username"}]
           [:input {:type "password" :name "password"}]
           [:button {:type "submit"} "Sign In"]]]]))

    :handle-unprocessable-entity
    (fn [{:keys [user-error redirect-error request]}]
      (if user-error
        (ring-response
          (html
            [:body
             [:h1 "Fehler"]
             [:p user-error]])
          {:status 200})
        (ring-response
          (html
            [:body
             [:h1 "Fehler"]
             [:p user-error]])
          {:status 302
           :headers {"Location" (location redirect-error request)}})))))

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
      (if-letk [[username] (ts/get-token token-store token)]
        {::resp
         {:active true
          :username username}}
        {::resp
         {:active false}}))

    :handle-ok ::resp))

(defnk sign-in-handler [token-store client-store authenticator]
  (resource
    :available-media-types ["text/html"]
    :allowed-methods [:post]
    :new? false
    :respond-with-entity? true

    :processable?
    (fnk [[:request params]]
      (let [{:keys [client_id redirect_uri]} params]
        (if-letk [[redirect-uris] (cs/get-client client-store client_id)]
          (if (some #(= redirect_uri %) redirect-uris)
            true
            [false {:error (str "Invalid redirection URI.")}])
          [false {:error "Invalid client identifier."}])))

    :post-redirect?
    (fnk [[:request [:params username password]]]
      (when-let [user-info (auth/check-credentials authenticator username password)]
        (let [token (str (UUID/randomUUID))]
          (ts/put-token! token-store token user-info)
          {:token token})))

    :location
    (fnk [[:request [:params redirect_uri {state nil}]] token]
      (cond-> (str redirect_uri "#access_token=" token "&token_type=bearer")
        state (str "&state=" state)))

    :handle-ok
    (fnk [[:request [:params client_id redirect_uri {state nil} username]]]
      (html
        [:html
         [:head [:title "Sign In"]]
         [:body
          [:h1 "Sign In"]
          [:form {:method "POST" :action (str "sign-in")}
           [:input {:type "hidden" :name "client_id" :value client_id}]
           [:input {:type "hidden" :name "redirect_uri" :value redirect_uri}]
           (when state [:input {:type "hidden" :name "state" :value state}])
           [:input {:type "text" :name "username" :value username}]
           [:input {:type "password" :name "password"}]
           [:button {:type "submit"} "Sign In"]]]]))

    :handle-unprocessable-entity
    (fnk [error]
      (ring-response
        (html
          [:body
           [:h1 "Fehler"]
           [:p error]])
        {:status 200}))))

(defn handlers [ctx]
  {:token (token-handler ctx)
   :authorization (authorization-handler ctx)
   :introspect (introspect-handler ctx)
   :sign-in (sign-in-handler ctx)})
