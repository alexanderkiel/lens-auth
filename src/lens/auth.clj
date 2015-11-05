(ns lens.auth)

(defprotocol Authenticator
  (check-credentials [this username password]))
