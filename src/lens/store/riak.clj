(ns lens.store.riak
  (:require [lens.store :refer [TokenStore]]
            [org.httpkit.client :as http]
            [clojure.string :as str]
            [com.stuartsierra.component :as component]
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
  component/Lifecycle
  (start [this]
    this)
  (stop [this]
    this)

  TokenStore
  (put! [_ token user-info]
    (riak-put! endpoint bucket token user-info))
  (get [_ token]
    (riak-get endpoint bucket token))
  (describe [_]
    (str "riak endpoint: " endpoint ", bucket: " bucket)))

(defn create-riak [host port bucket]
  (->Riak (riak-endpoint host port) bucket))
