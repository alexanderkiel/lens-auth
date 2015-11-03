(ns lens.store.expire-test
  (:require [clojure.test :refer :all]
            [lens.store.expire :refer :all]
            [lens.util :refer [now]]))

(deftest expired?-test
  (testing "Passing nil yields false"
    (is (not (expired? nil))))

  (testing "Passing now yields true"
    (is (expired? {:expires (now)})))

  (testing "Passing past yields true"
    (is (expired? {:expires (dec (now))})))

  (testing "Passing future yields false"
    (is (not (expired? {:expires (+ (now) 1000)})))))
