(ns flows.get-trace-graph
  (:require [common-clj.state-flow-helpers.http-client :as http-client]
            [common-clj.state-flow-helpers.http-server :refer [GET]]
            [flows.aux.init :refer [defflow]]
            [matcher-combinators.matchers :as m]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(def trace
  {"data" [{"traceID"   "1"
            "spans"     [{"traceID"       "1"
                          "spanID"        "span-1-1"
                          "processID"     "p1"
                          "operationName" "http.out /api/orders"
                          "startTime"     1499999999999999
                          "references"    []
                          "tags"          []
                          "duration"      1000}
                         {"traceID"       "1"
                          "spanID"        "span-1-2"
                          "processID"     "p2"
                          "operationName" "http.in /api/orders"
                          "startTime"     1500000000000000
                          "references"    [{"ref-type" "CHILD_OF"
                                            "trace-id" "1"
                                            "span-id"  "span-1-1"}]
                          "tags"          []
                          "duration"      1001}]
            "processes" {"p1" {"serviceName" "frontend"}
                         "p2" {"serviceName" "orders"}}}]})

(def mock-http-calls
  {"{{jaeger}}/api/traces/1" {:status 200 :body trace}})

(defflow get-trace-graph
  :pre-conditions [(http-client/mock! mock-http-calls)]

  [response (GET "/api/traces/1/graph")]

  (match? {:status 200
           :body {"graph" {"nodes" (m/in-any-order [{"service" "frontend"}
                                                    {"service" "orders"}])
                           "edges" [{"from" "frontend"
                                     "to"   "orders"}]}}}
          response))
