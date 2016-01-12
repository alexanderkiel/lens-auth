(ns lens.store
  (:require [schema.core :refer [Str]])
  (:refer-clojure :exclude [get]))

(def UserInfo
  {:username Str})

(defprotocol TokenStore
  "A simple key-value token store."
  (put! [this token user-info] "Puts user-info under token.")
  (get [this token] "Returns user-info of token."))
