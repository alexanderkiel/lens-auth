(ns lens.store.atom
  (:use plumbing.core)
  (:require [lens.store :refer [TokenStore]]
            [lens.descriptive :refer [Descriptive]]
            [com.stuartsierra.component :as component]
            [lens.util :refer [now]]
            [lens.store.expire :refer [expired? Sec]]))

(deftype Atom [expire db]
  component/Lifecycle
  (start [_]
    (Atom. expire (atom {})))

  (stop [_]
    (Atom. expire nil))

  TokenStore
  (put! [_ token user-info]
    (let [expires (+ (now) expire)
          value (assoc user-info :expires expires)]
      (swap! db #(assoc % token value))))

  (get [_ token]
    (let [user-info (get-in @db [token])]
      (if (expired? user-info)
        (do (swap! db #(dissoc % token)) nil)
        user-info)))

  Descriptive
  (describe [_]
    "in-memory atom (ephemeral)"))

(defnk create-atom [expire :- Sec]
  (->Atom (* expire 1000) nil))
