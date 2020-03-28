(ns atlas.domain.sequence-diagram-test
  (:require [atlas.domain.sequence-diagram :as nut]
            [clojure.test :refer [is testing]]
            [common-clj.clojure-test-helpers.core :refer [deftest]]))

(def trace
  {:trace-id "1"
   :spans    [{:trace-id       "1"
               :span-id        "1"
               :process-id     :p1
               :operation-name "http.in GET /api/orders/1"
               :start-time     1500000000000000
               :duration       1000
               :references     []
               :tags          [{:key   "span.kind"
                                :type  "string"
                                :value "server"}
                               {:key   "http.method"
                                :type  "string"
                                :value "GET"}
                               {:key   "http.url"
                                :type  "string"
                                :value "/api/orders/1"}]}

              {:trace-id       "1"
               :span-id        "2"
               :process-id     :p1
               :operation-name "http.out GET /api/orders/1"
               :start-time     1500000000100000
               :duration       300
               :references     [{:ref-type :child-of
                                 :trace-id "1"
                                 :span-id  "1"}]
               :tags          [{:key   "span.kind"
                                :type  "string"
                                :value "client"}
                               {:key   "http.method"
                                :type  "string"
                                :value "GET"}
                               {:key   "http.url"
                                :type  "string"
                                :value "/api/orders/1"}]}

              {:trace-id       "1"
               :span-id        "3"
               :process-id     :p2
               :operation-name "http.in GET /api/orders/1"
               :start-time     1500000000200000
               :duration       100
               :references     [{:ref-type :child-of
                                 :trace-id "1"
                                 :span-id  "2"}]
               :tags          [{:key   "span.kind"
                                :type  "string"
                                :value "server"}
                               {:key   "http.method"
                                :type  "string"
                                :value "GET"}
                               {:key   "http.url"
                                :type  "string"
                                :value "/api/orders/1"}]}]

   :processes {:p1 {:service-name "bff"}
               :p2 {:service-name "orders"}}})

(deftest start-time
  (testing "returns the oldest span start time"
    (is (= #epoch 1500000000000
           (nut/start-time trace)))))

(deftest duration-ms
  (testing "time between the beginning of the first started span and the end of the last finished span"
    (is (= 1000
           (nut/duration-ms trace)))))

(deftest lifelines
  (testing "builds lifelines from trace"
    (is (= [{:name "bff"}
            {:name "orders"}]
           (nut/lifelines trace)))))

(deftest execution-boxes
  (testing "builds execution boxes from trace"
    (is (= [{:id          "1"
             :start-time  #epoch 1500000000000
             :duration-ms 1000
             :lifeline    "bff"}
            {:id          "3"
             :start-time  #epoch 1500000000200
             :duration-ms 100
             :lifeline    "orders"}]
           (nut/execution-boxes trace)))))

(deftest arrows
  (testing "builds arrows from trace"
    (is (= [{:id         "2"
             :from       "bff"
             :to         "orders"
             :start-time #epoch 1500000000100}
            {:id         "3"
             :from       "orders"
             :to         "bff"
             :start-time #epoch 1500000000400}]
           (nut/arrows trace)))))
