(ns lens.store
  (:require [schema.core :refer [Str]]))

(def UserInfo
  {:username Str})

(defprotocol TokenStore
  "A simple key-value token store."
  (get-token [this token] "Returns user-info of token.")
  (put-token! [this token user-info] "Puts user-info under token."))
