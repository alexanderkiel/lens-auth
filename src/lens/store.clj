(ns lens.store
  (:require [schema.core :refer [Str]]))

(def UserInfo
  {:username Str})

(defprotocol TokenStore
  "A simple key-value token store."
  (put-token! [this token user-info] "Puts user-info under token.")
  (get-token [this token] "Returns user-info of token."))
