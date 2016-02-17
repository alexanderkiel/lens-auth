(ns lens.handler
  (:use plumbing.core)
  (:require [clojure.string :as str]
            [liberator.core :refer [resource]]
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

(defn- html-head [title]
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   [:link {:rel "stylesheet"
           :href "https://fonts.googleapis.com/css?family=Open+Sans:400,600,300,700"}]
   [:link {:rel "stylesheet"
           :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
           :integrity "sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7"
           :crossorigin "anonymous"}]
   [:style "body {margin-top: 72px}"]
   [:title title]])

(defn render-header [i18n]
  [:header {:class "navbar navbar-default navbar-fixed-top"}
   [:div {:class "container-fluid"}
    [:div {:class "navbar-header"}
     [:a {:href "#" :class "navbar-brand"} (i18n :navbar-brand-label)]]]])

(defn render-sign-in-form [path-for i18n client_id redirect_uri state username 
                           wrong-credentials]
  [:form {:method "POST" :action (path-for :sign-in)}
   [:input {:type "hidden" :name "client_id" :value client_id}]
   [:input {:type "hidden" :name "redirect_uri" :value redirect_uri}]
   (when state [:input {:type "hidden" :name "state" :value state}])
   [:div {:class (cond-> "form-group" wrong-credentials (str " has-error"))}
    [:label {:for "username" :class "control-label"} (i18n :username)]
    [:input {:type "text" :id "username" :name "username"
             :class "form-control" :autofocus "autofocus" :value username}]]
   [:div {:class (cond-> "form-group" wrong-credentials (str " has-error"))}
    [:label {:for "password" :class "control-label"} (i18n :password)]
    [:input {:type "password" :id "password" :name "password"
             :class "form-control"}]
    (when wrong-credentials
      [:span {:class "help-block"} (i18n :wrong-credentials-msg)])]
   [:button {:type "submit" :class "btn btn-default"} 
    (i18n :sign-in-button-label)]])

(defn render-sign-in-page [path-for i18n client_id redirect_uri state username 
                           wrong-credentials]
  (html
    [:html
     (html-head (i18n :sign-in-title))
     [:body
      (render-header i18n)
      [:div {:class "container"}
       [:div {:class "row"}
        [:div {:class 
               "col-xs-12 col-sm-offset-3 col-sm-6 col-md-offset-4 col-md-4"}
         [:h1 (i18n :sign-in-title)]
         (render-sign-in-form path-for i18n client_id redirect_uri state 
                              username wrong-credentials)]]]]]))

(defnk authorization-handler
  "Implements the authorization endpoint as described in RFC 6749 Section 4.2.1.

  The following request parameters are supported:

    response_type - Value MUST be set to \"token\".
    client_id     - The client identifier as described in Section 2.2.
    redirect_uri  - As described in Section 3.1.2.
    state         - An opaque value used by the client to maintain state between
                    the request and callback. (optional)"
  [client-store i18n]
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
    (fnk [[:request path-for [:params client_id redirect_uri {state nil} 
                              {username nil} {wrong_credentials false}]]]
      (render-sign-in-page path-for i18n client_id redirect_uri state username 
                           wrong_credentials))

    :handle-unprocessable-entity
    (fn [{:keys [user-error redirect-error request]}]
      (if user-error
        (ring-response
          (html
            [:html
             (html-head "Sign-In")
             [:body
              [:h1 "Fehler"]
              [:p user-error]]])
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
  "Handles POST request from the sign-in form.
  
  Redirects to the authorization endpoint on wrong password."
  (resource
    :available-media-types ["text/html"]
    :allowed-methods [:post]

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
      (if-let [user-info (auth/check-credentials authenticator username password)]
        (let [token (str (UUID/randomUUID))]
          (ts/put-token! token-store token user-info)
          {:token token})
        {}))

    :location
    (fnk [[:request path-for [:params client_id redirect_uri {state nil} 
                              {username nil}]] 
          {token nil}]
      (if token
        (cond-> (str redirect_uri "#access_token=" token "&token_type=bearer")
          state (str "&state=" state))
        (cond-> (str (path-for :authorization) "?response_type=token"
                     "&client_id=" client_id "&redirect_uri=" redirect_uri
                     "&wrong_credentials=true")
          state (str "&state=" state)
          (not (str/blank? username)) (str "&username=" username))))

    :handle-unprocessable-entity
    (fnk [error]
      (ring-response
        (html
          [:html
           (html-head "Sign In")
           [:body
            [:h1 "Fehler"]
            [:p error]]])
        {:status 200}))))

(defn handlers [ctx]
  {:token (token-handler ctx)
   :authorization (authorization-handler ctx)
   :introspect (introspect-handler ctx)
   :sign-in (sign-in-handler ctx)})
