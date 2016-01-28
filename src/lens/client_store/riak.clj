(ns lens.client-store.riak
  (:use plumbing.core)
  (:require [lens.client-store :refer [ClientStore]]
            [lens.descriptive :refer [Descriptive]]
            [org.httpkit.client :as http]
            [clojure.string :as str]
            [com.stuartsierra.component :as comp]
            [clojure.data.json :as json]))

(defn- riak-endpoint [host port]
  (str "http://" host ":" port "/riak"))

(def ^:private headers
  {"Content-Type" "application/json"})

(def ^:private riak-key (partial str/join "/"))

(defn- riak-put! [endpoint bucket key body]
  (let [key-url (riak-key [endpoint bucket key])
        body (json/write-str body)]
    @(http/post key-url {:headers headers :body body})))

(defn- riak-get [endpoint bucket key]
  (let [key-url (riak-key [endpoint bucket key])
        response @(http/get key-url)]
    (when (= (:status response) 200)
      (json/read-str (:body response) :key-fn keyword))))

(deftype Riak [endpoint bucket]
  comp/Lifecycle
  (start [this]
    this)
  (stop [this]
    this)

  ClientStore
  (get-client [_ client-id]
    (when client-id (riak-get endpoint bucket client-id)))

  (put-client! [_ client-id client]
    (riak-put! endpoint bucket client-id client))

  Descriptive
  (describe [_]
    (str "riak endpoint: " endpoint ", bucket: " bucket)))

(defnk create-riak
  "Creates a Riak client store."
  [riak-client-host {riak-client-port "8098"}
   {riak-client-bucket "auth-clients"}]
  (->Riak (riak-endpoint riak-client-host riak-client-port) riak-client-bucket))
