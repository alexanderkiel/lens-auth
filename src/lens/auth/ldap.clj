(ns lens.auth.ldap
  (:use plumbing.core)
  (:require [clj-ldap.client :as ldap]
            [com.stuartsierra.component :as comp]
            [lens.auth :refer [Authenticator]]
            [lens.descriptive :refer [Descriptive]]
            [clojure.string :as str]))

(def ^:private search-opts
  {:attributes [:sAMAccountName :dn] :scope :one})

(defn coerce-username
  "Tries to coerce a valid username from s.
   Strips a possible domains and NTLM names.
   Returns nil on invalid usernames."
  [s]
  (when (string? s)
    (let [[s] (-> s str/trim str/lower-case (str/split #"@"))]
      (when s
        (let [[_ s] (conj (str/split s #"\\") s)]
          (when (and s (re-matches #"[\w-]+" s))
            s))))))

(defrecord Ldap [hosts base-dn bind-dn bind-pw search-tpl conn]
  comp/Lifecycle
  (start [ldap]
    (let [conn (ldap/connect {:host hosts :bind-dn bind-dn :password bind-pw})]
      (assoc ldap :conn conn)))

  (stop [ldap]
    (assoc ldap :conn nil))

  Authenticator
  (check-credentials* [_ username password]
    (when conn
      (when-let [username (coerce-username username)]
        (let [opts (assoc search-opts :filter (format search-tpl username))
              [user & others] (ldap/search conn base-dn opts)]
          (when (empty? others)
            (when (and user (ldap/bind? conn (:dn user) password))
              {:username (:sAMAccountName user)}))))))

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
