(ns lens.store.expire
  (:require [lens.util :refer [now]]
            [schema.core :as s]))

(def Sec
  "A second."
  (s/both s/Int (s/pred #(not (neg? %)) 'not-neg)))

(defn expired? [{:keys [expires]}]
  (and expires (>= (now) expires)))
