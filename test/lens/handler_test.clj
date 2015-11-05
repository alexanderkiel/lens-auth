(ns lens.handler-test
  (:require [clojure.test :refer :all]
            [lens.handler :refer :all]
            [lens.test-util :refer :all]
            [clojure.data.json :as json]
            [lens.util :refer [now]]))

(deftest token-handler-test
  (let [db (atom {})
        resp (execute (token-handler (create-ctx db)) :post
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

(deftest invalid-credential-test
  (let [resp (execute (token-handler
                        (create-ctx (atom {}) (->NoopWith false))) :post
               :params {:grant_type "password"
                        :username "name-153440"
                        :password "pwd-153450"})]

    (testing "Invalid credentials are a bad request"
      (is (= 400 (:status resp))))

    (testing "Invalid credentials error is invalid_grant"
      (is (= "invalid_grant" ((json/read-str (:body resp)) "error"))))))

(deftest invalid-request-test
  (let [resp (execute (token-handler
                        (create-ctx (atom {}) (->NoopWith false))) :post
               :params {})]

    (is (= 400 (:status resp)))

    (is (= "invalid_request" ((json/read-str (:body resp)) "error")))))

(deftest invalid-grant-type-test
  (let [resp (execute (token-handler
                        (create-ctx (atom {}) (->NoopWith false))) :post
               :params {:username "not-nil" :password "not-nil" :grant_type "not_password"})]

    (is (= 400 (:status resp)))

    (is (= "unsupported_grant_type" ((json/read-str (:body resp)) "error")))))

(deftest introspect-handler-test
  (let [db (atom {"token-160337" {:username "name-160347"
                                  :expires (+ (now) 2000)}})
        resp (execute (introspect-handler (create-ctx db)) :post
               :params {:token "token-160337"})]

    (is (= 200 (:status resp)))

    (testing "Response is active"
      (is ((json/read-str (:body resp)) "active")))

    (testing "Response contains the username"
      (is (= "name-160347" ((json/read-str (:body resp)) "username"))))))

(deftest expire-test
  (let [db (atom {"token-78927"
                  {:username "name-890562"
                   :expires (- (now) 2000)}})
        expired (execute (introspect-handler (create-ctx db)) :post
                  :params {:token "token-78927"})]

    (is (= 200 (:status expired)))

    (testing "Response is inactive"
      (is (not ((json/read-str (:body expired)) "active"))))

    (testing "Response does not contain a username"
      (is (nil? ((json/read-str (:body expired)) "username"))))

    (testing "DB does not contain token anymore"
      (is (not (contains? @db "token-78927"))))))
