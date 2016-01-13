(ns lens.client-store
  (:require [schema.core :refer [Str]]))

(def Client
  {:redirect-uris [Str]})

(defprotocol ClientStore
  "A simple key-value store for clients holding there registered redirect URIs."
  (get-client [this client-id] "Returns the client of client-id if there is any.")
  (put-client! [this client-id client] "Puts the client under client-id."))
