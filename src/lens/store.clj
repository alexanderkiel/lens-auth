(ns lens.store
  (:refer-clojure :exclude [get]))

(defprotocol TokenStore
  (put! [this token user-info])
  (get [this token])
  (describe [this]))
