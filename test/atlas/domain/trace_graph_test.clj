(ns atlas.domain.trace-graph-test
  (:require [atlas.domain.trace-graph :as nut]
            [clojure.test :refer [is testing]]
            [common-clj.clojure-test-helpers.core :refer [deftest]]))

(deftest build-graph
  (testing "builds graph from trace"
    (is (= {:nodes #{{:service "frontend"}
                     {:service "orders"}}
            :edges #{{:from "frontend"
                      :to   "orders"}}}
           (nut/build-graph {:trace-id  "1"
                             :spans     [{:trace-id       "1"
                                          :span-id        "span-1-1"
                                          :process-id     :p1
                                          :operation-name "http.out /api/orders"
                                          :start-time     1499999999999999
                                          :references     []
                                          :duration       1000}
                                         {:trace-id       "1"
                                          :span-id        "span-1-2"
                                          :process-id     :p2
                                          :operation-name "http.in /api/orders"
                                          :start-time     1500000000000000
                                          :references     [{:ref-type :child-of
                                                            :trace-id "1"
                                                            :span-id  "span-1-1"}]
                                          :duration       1001}]
                             :processes {:p1 {:service-name "frontend"}
                                         :p2 {:service-name "orders"}}})))))
