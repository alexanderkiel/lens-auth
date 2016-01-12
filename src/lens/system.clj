(ns lens.system
  (:use plumbing.core)
  (:require [org.httpkit.server :refer [run-server]]
            [lens.util :refer [parse-long]]
            [lens.app :refer [app]]
            [lens.server :refer [new-server]]
            [lens.store.atom :refer [create-atom]]
            [lens.store.riak :refer [create-riak]]
            [com.stuartsierra.component :as comp]
            [lens.auth.noop :refer [create-noop]]
            [lens.auth.ldap :refer [create-ldap]]))

(defn- create-token-store [{:keys [token-store] :as env}]
  (let [env (update env :expire (fnil parse-long "3600"))]
    (case (some-> token-store .toLowerCase)
      "riak" (create-riak env)
      (create-atom env))))

(defn- create-authenticator [{:keys [auth] :as env}]
  (case (some-> auth .toLowerCase)
    "ldap" (create-ldap env)
    (create-noop)))

(defnk new-system [lens-auth-version :as env]
  (comp/system-map
    :version lens-auth-version
    :token-store (create-token-store env)
    :authenticator (create-authenticator env)
    :server (comp/using (new-server env) [:token-store :authenticator])))
