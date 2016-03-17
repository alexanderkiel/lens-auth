(ns lens.auth.ldap-test
  (:require [clojure.test :refer :all]
            [lens.auth.ldap :refer :all]))

(deftest split-hosts-test
  (are [s res] (= res (split-hosts s))
    "h0" ["h0"]
    "h0,h1" ["h0" "h1"]
    "h0 h1" ["h0" "h1"]
    "h0, h1" ["h0" "h1"]))

(deftest coerce-username-test
  (is (nil? (coerce-username nil)))
  (is (nil? (coerce-username "")))
  (is (nil? (coerce-username "@")))
  (is (nil? (coerce-username "\\")))
  (is (nil? (coerce-username "*")))
  (is (= "a" (coerce-username "a")))
  (is (= "1" (coerce-username "1")))
  (is (= "-" (coerce-username "-")))
  (is (= "_" (coerce-username "_")))
  (is (= "aa" (coerce-username "aa")))
  (testing "strips domain names"
    (is (= "a" (coerce-username "a@")))
    (is (= "a" (coerce-username "a@x"))))
  (testing "strips NTLM names"
    (is (= "a" (coerce-username "\\a")))
    (is (= "a" (coerce-username "x\\a")))))
