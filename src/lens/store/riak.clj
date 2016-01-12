(ns lens.store.riak
  (:use plumbing.core)
  (:require [lens.store :refer [TokenStore]]
            [lens.descriptive :refer [Descriptive]]
            [org.httpkit.client :as http]
            [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [clojure.data.json :as json]
            [lens.util :as u :refer [now]]
            [lens.store.expire :refer [expired? Sec]]))

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

(defn- riak-delete! [endpoint bucket key]
  (let [key-url (riak-key [endpoint bucket key])]
    @(http/delete key-url)))

(defrecord Riak [endpoint bucket expire]
  component/Lifecycle
  (start [this]
    this)

  (stop [this]
    this)

  TokenStore
  (get-token [_ token]
    (let [user-info (riak-get endpoint bucket token)]
      (if (expired? user-info)
        (do (riak-delete! endpoint bucket token) nil)
        user-info)))

  (put-token! [_ token user-info]
    (->> (assoc user-info :expires (+ (now) expire))
         (riak-put! endpoint bucket token)))

  Descriptive
  (describe [_]
    (str "riak endpoint: " endpoint ", bucket: " bucket)))

(defnk create-riak
  "Creates a Riak token store."
  [riak-host {riak-port "8098"} {riak-bucket "auth"} expire :- Sec]
  (->Riak (riak-endpoint riak-host riak-port) riak-bucket (* expire 1000)))
