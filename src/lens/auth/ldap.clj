(ns lens.auth.ldap
  (:use plumbing.core)
  (:require [clj-ldap.client :as ldap]
            [com.stuartsierra.component :as component]
            [lens.auth :refer [Authenticator]]
            [lens.descriptive :refer [Descriptive]]
            [clojure.string :as str]))

(deftype Ldap [hosts base-dn conn]
  component/Lifecycle
  (start [_]
    (Ldap. hosts base-dn (ldap/connect {:host hosts})))

  (stop [_]
    (Ldap. hosts base-dn nil))

  Authenticator
  (check-credentials [_ username password]
    (let [bind-dn (str "CN=" username "," base-dn)]
      (ldap/bind? conn bind-dn password)))

  Descriptive
  (describe [_]
    (str "ldap on " hosts " and user-base: " base-dn)))

(defnk create-ldap [ldap-hosts ldap-user-base-dn]
  (let [hosts (str/split (str/trim ldap-hosts) #"[\s,]")]
    (->Ldap hosts ldap-user-base-dn nil)))
