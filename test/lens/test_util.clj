(ns lens.test-util
  (:require [lens.token-store.atom :refer [->Atom]]
            [lens.auth.noop :refer [->Noop]]
            [lens.auth :refer [Authenticator]]))

(defn request [method & kvs]
  (reduce-kv
    (fn [m k v]
      (if (sequential? k)
        (assoc-in m k v)
        (assoc m k v)))
    {:request-method method
     :headers {"accept" "*/*"}
     :params {}}
    (apply hash-map kvs)))

(defn execute [handler method & kvs]
  (handler (apply request method kvs)))

(defn create-ctx
  ([db] (create-ctx db (->Noop)))
  ([db auth] {:token-store (->Atom 1 db)
              :authenticator auth}))

(deftype NoopWith [response]
  Authenticator
  (check-credentials [_ _ _]
    response))
