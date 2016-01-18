(ns lens.auth
  (:require [schema.core :as s]))

(s/def UserInfo
  {:username s/Str})

(defprotocol Authenticator
  (check-credentials* [this username password]))

(s/defn check-credentials :- (s/maybe UserInfo) [auth username password]
  "Checks whether the username and password combination is valid and returns
  user information."
  (check-credentials* auth username password))
