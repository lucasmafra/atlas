(ns atlas.domain.trace-search-test
  (:require [atlas.domain.trace-search :as nut]
            [clojure.test :refer [deftest is testing]]))

(def trace
  {:trace-i-d "trace-1"
   :spans     [{:trace-id      "trace-1"
                :span-id       "span-1-1"
                :process-id    "p1"
                :operation-name "op-1-1"
                :start-time     1500000000000000
                :duration       1000}
               {:trace-id      "trace-1"
                :span-id       "span-1-2"
                :process-id    "p2"
                :operation-name "op-1-2"
                :start-time     1400000000000000
                :duration       1001}]
   :processes {:p1 {:service-name "orders"}
               :p2 {:service-name "feed"}}})

(deftest total-spans
  (testing "returns total spans for trace"
    (is (= 2
           (nut/total-spans trace)))))

(deftest duration-ms
  (testing "returns sum of all spans' duration in milliseconds"
    (is (= 2001
           (nut/duration-ms trace)))))

(deftest start-time
  (testing "returns the epoch millis corresponding to the oldest span start-time"
    (is (= #epoch 1400000000000
           (nut/start-time trace)))))

(deftest services-summary
  (testing "returns the summary of all services related to the trace"
    (is (= [{:name            "orders"
             :number-of-spans 1}
            {:name            "feed"
             :number-of-spans 1}]
           (nut/services-summary trace)))))
