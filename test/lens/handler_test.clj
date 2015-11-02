(ns lens.handler-test
  (:require [clojure.test :refer :all]
            [lens.handler :refer :all]
            [lens.test-util :refer :all]
            [clojure.data.json :as json]))

(deftest token-handler-test
  (let [db (atom {})
        resp (execute (token-handler db) :post
               :params {:grant_type "password"
                        :username "name-153440"
                        :password "pwd-153450"})]

    (is (= 200 (:status resp)))

    (testing "Response is declared not cacheable"
      (is (= "no-store" (get-in resp [:headers "cache-control"])))
      (is (= "no-cache" (get-in resp [:headers "pragma"]))))

    (testing "Token type is bearer"
      (is (= "bearer" ((json/read-str (:body resp)) "token_type"))))

    (testing "Response contains the generated token"
      (is (= (first (keys @db))
             ((json/read-str (:body resp)) "access_token"))))))

(deftest introspect-handler-test
  (let [db (atom {"token-160337" {:username "name-160347"}})
        resp (execute (introspect-handler db) :post
               :params {:token "token-160337"})]

    (is (= 200 (:status resp)))

    (testing "Response is active"
      (is ((json/read-str (:body resp)) "active")))

    (testing "Response contains the username"
      (is (= "name-160347" ((json/read-str (:body resp)) "username"))))))
