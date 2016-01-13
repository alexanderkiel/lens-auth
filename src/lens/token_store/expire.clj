(ns lens.token-store.expire
  (:require [lens.util :refer [now]]
            [schema.core :as s]))

(def Sec
  "A second."
  (s/constrained s/Int (comp not neg?) 'not-neg?))

(defn expired? [{:keys [expires]}]
  (and expires (>= (now) expires)))
