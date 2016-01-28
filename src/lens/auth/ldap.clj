(ns lens.auth.ldap
  (:use plumbing.core)
  (:require [clj-ldap.client :as ldap]
            [com.stuartsierra.component :as comp]
            [lens.auth :refer [Authenticator]]
            [lens.descriptive :refer [Descriptive]]
            [clojure.string :as str]))

(def ^:private search-opts
  {:attributes [:sAMAccountName :dn] :scope :one})

(defrecord Ldap [hosts base-dn bind-dn bind-pw search-tpl conn]
  comp/Lifecycle
  (start [_]
    (let [conn (ldap/connect {:host hosts :bind-dn bind-dn :password bind-pw})]
      (Ldap. hosts base-dn bind-dn bind-pw search-tpl conn)))

  (stop [_]
    (Ldap. hosts base-dn bind-dn bind-pw search-tpl nil))

  Authenticator
  (check-credentials* [_ username password]
    (let [opts (assoc search-opts :filter (format search-tpl username))
          user (first (ldap/search conn base-dn opts))]
      (when (and user (ldap/bind? conn (:dn user) password))
        {:username (:sAMAccountName user)})))

  Descriptive
  (describe [_]
    (str "ldap on " hosts " and user-base: " base-dn)))

(defn split-hosts [ldap-hosts]
  (str/split (str/trim ldap-hosts) #"[\s,]+"))

(defnk create-ldap [ldap-hosts ldap-bind-dn ldap-bind-password ldap-user-base-dn
                    {ldap-search-tpl "(sAMAccountName=%s)"}]
  (let [hosts (split-hosts ldap-hosts)]
    (->Ldap hosts ldap-user-base-dn ldap-bind-dn ldap-bind-password
            ldap-search-tpl nil)))
