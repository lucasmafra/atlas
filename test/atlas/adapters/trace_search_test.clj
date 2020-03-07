(ns atlas.adapters.trace-search-test
  (:require [atlas.adapters.trace-search :as nut]
            [clojure.test :refer [is testing]]
            [common-clj.clojure-test-helpers.core :refer [deftest]]))

(deftest ->jaeger-search-trace-query
  (testing "converts correctly"
    (is (= {:service   "service"
            :operation "operation"
            :limit     10
            :start     1581797873000000
            :end       1582769681000000}
           (nut/->jaeger-search-trace-query
            {:service   "service"
             :operation "operation"
             :limit     10
             :start     #epoch 1581797873000
             :end       #epoch 1582769681000})))))

(deftest ->trace-summary
  (testing "converts correctly"
    (is (= {:trace-id    "trace-id"
            :total-spans 10
            :duration-ms 1000
            :start-time  #epoch 1581797873000
            :services    [{:name            "orders"
                           :number-of-spans 10}]}
           (nut/->trace-summary
            "trace-id" 10 1000 #epoch 1581797873000 [{:name "orders" :number-of-spans 10}])))))
