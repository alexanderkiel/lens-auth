(ns lens.route
  (:require [bidi.bidi :as bidi]))

(def routes
  ["/" {"token" :token
        "introspect" :introspect}])

(defn path-for [handler & params]
  (apply bidi/path-for routes handler params))
