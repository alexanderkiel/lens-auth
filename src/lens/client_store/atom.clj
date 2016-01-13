(ns lens.client-store.atom
  (:use plumbing.core)
  (:require [lens.client-store :refer [ClientStore]]
            [lens.descriptive :refer [Descriptive]]
            [com.stuartsierra.component :as comp]))

(defrecord Atom [db]
  comp/Lifecycle
  (start [this]
    (assoc this :db (atom {})))

  (stop [this]
    (assoc this :db nil))

  ClientStore
  (get-client [_ client-id]
    (@db client-id))

  (put-client! [_ client-id client]
    (swap! db assoc client-id client))

  Descriptive
  (describe [_]
    "in-memory atom (ephemeral)"))

(defnk create-atom
  "Creates an atom client store."
  []
  (->Atom nil))
