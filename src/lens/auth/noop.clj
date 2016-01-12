(ns lens.auth.noop
  (:require [com.stuartsierra.component :as component]
            [lens.auth :refer [Authenticator]]
            [lens.descriptive :refer [Descriptive]]))

(deftype Noop []
  component/Lifecycle
  (start [this]
    this)

  (stop [this]
    this)

  Authenticator
  (check-credentials [_ _ _]
    true)

  Descriptive
  (describe [_]
    "noop (all credentials are valid)"))

(defn create-noop []
  (->Noop))