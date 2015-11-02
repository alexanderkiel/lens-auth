(ns lens.store.atom
  (:require [lens.store :refer [TokenStore]]
            [com.stuartsierra.component :as component]))

(deftype Atom [db]
  component/Lifecycle
  (start [_]
    (Atom. (atom {})))
  (stop [_]
    (Atom. nil))

  TokenStore
  (put! [_ token user-info]
    (swap! db #(assoc % token user-info)))
  (get [_ token]
    (get-in @db [token]))
  (describe [_]
    "in-memory atom (ephemeral)"))

(defn create-atom []
  (->Atom nil))
