(ns lens.token-store.atom
  (:use plumbing.core)
  (:require [lens.token-store :refer [TokenStore]]
            [lens.descriptive :refer [Descriptive]]
            [com.stuartsierra.component :as comp]
            [lens.util :refer [now]]
            [lens.token-store.expire :refer [expired? Sec]]))

(defrecord Atom [expire db]
  comp/Lifecycle
  (start [this]
    (assoc this :db (atom {})))

  (stop [this]
    (assoc this :db nil))

  TokenStore
  (get-token [_ token]
    (let [user-info (@db token)]
      (if (expired? user-info)
        (do (swap! db dissoc token) nil)
        user-info)))

  (put-token! [_ token user-info]
    (let [expires (+ (now) expire)
          value (assoc user-info :expires expires)]
      (swap! db assoc token value)))

  Descriptive
  (describe [_]
    "in-memory atom (ephemeral)"))

(defnk create-atom
  "Creates an atom token store."
  [expire :- Sec]
  (->Atom (* expire 1000) nil))
