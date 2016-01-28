(ns lens.auth.ldap-test
  (:require [clojure.test :refer :all]
            [lens.auth.ldap :refer :all]))

(deftest split-hosts-test
  (are [s res] (= res (split-hosts s))
    "h0" ["h0"]
    "h0,h1" ["h0" "h1"]
    "h0 h1" ["h0" "h1"]
    "h0, h1" ["h0" "h1"]))
