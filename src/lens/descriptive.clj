(ns lens.descriptive)

(defprotocol Descriptive
  (describe [this] "Humand readable description"))
