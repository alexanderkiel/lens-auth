(ns lens.system
  (:use plumbing.core)
  (:require [org.httpkit.server :refer [run-server]]
            [com.stuartsierra.component :as comp]
            [lens.util :refer [parse-long]]
            [lens.app :refer [app]]
            [lens.server :refer [new-server]]
            [lens.token-store.atom :as token-atom]
            [lens.token-store.riak :as token-riak]
            [lens.client-store.atom :as client-atom]
            [lens.client-store.riak :as client-riak]
            [lens.auth.noop :refer [create-noop]]
            [lens.auth.ldap :refer [create-ldap]]))

(defn- create-token-store [{:keys [token-store] :as env}]
  (let [env (update env :expire (fnil parse-long "3600"))]
    (case (some-> token-store .toLowerCase)
      "riak" (token-riak/create-riak env)
      (token-atom/create-atom env))))

(defn- create-client-store [{:keys [client-store] :as env}]
  (case (some-> client-store .toLowerCase)
    "riak" (client-riak/create-riak env)
    (client-atom/create-atom env)))

(defn- create-authenticator [{:keys [auth] :as env}]
  (case (some-> auth .toLowerCase)
    "ldap" (create-ldap env)
    (create-noop)))

(defnk new-system [lens-auth-version {i18n-name "default"} :as env]
  (comp/system-map
    :version lens-auth-version
    :token-store (create-token-store env)
    :client-store (create-client-store env)
    :authenticator (create-authenticator env)
    :i18n-name i18n-name
    :server (comp/using (new-server env) [:token-store :client-store
                                          :authenticator :i18n-name])))
