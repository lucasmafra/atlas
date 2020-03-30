(ns flows.get-sequence-diagram
  (:require [common-clj.state-flow-helpers.http-client :as http-client]
            [common-clj.state-flow-helpers.http-server :refer [GET]]
            [flows.aux.init :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(def jaeger-response
  {"data" [{"traceID" "1"
            "spans"   [{"traceID"       "1"
                        "spanID"        "1"
                        "processID"     "p1"
                        "operationName" "http.in GET /api/orders/1"
                        "startTime"     1500000000000000
                        "duration"      1000
                        "references"    []
                        "tags"          [{"key"   "span.kind"
                                          "type"  "string"
                                          "value" "server"}
                                         {"key"   "http.method"
                                          "type"  "string"
                                          "value" "GET"}
                                         {"key"   "http.url"
                                          "type"  "string"
                                          "value" "/api/orders/1"}]}

                       {"traceID"       "1"
                        "spanID"        "2"
                        "processID"     "p1"
                        "operationName" "http.out GET /api/orders/1"
                        "startTime"     1500000000100000
                        "duration"      300
                        "references"    [{"ref-type" "CHILD_OF"
                                          "traceID"  "1"
                                          "spanID"   "1"}]
                        "tags"          [{"key"   "span.kind"
                                          "type"  "string"
                                          "value" "client"}
                                         {"key"   "http.method"
                                          "type"  "string"
                                          "value" "GET"}
                                         {"key"   "http.url"
                                          "type"  "string"
                                          "value" "/api/orders/1"}]}

                       {"traceID"       "1"
                        "spanID"        "3"
                        "processID"     "p2"
                        "operationName" "http.in GET /api/orders/1"
                        "startTime"     1500000000200000
                        "duration"      100
                        "references"    [{"ref-type" "CHILD_OF"
                                          "traceID"  "1"
                                          "spanID"   "2"}]
                        "tags"          [{"key"   "span.kind"
                                          "type"  "string"
                                          "value" "server"}
                                         {"key"   "http.method"
                                          "type"  "string"
                                          "value" "GET"}
                                         {"key"   "http.url"
                                          "type"  "string"
                                          "value" "/api/orders/1"}]}]

            "processes" {"p1" {"serviceName" "bff"}
                         "p2" {"serviceName" "orders"}}}]})

(defflow get-sequence-diagram
  :pre-conditions [(http-client/mock! {"{{jaeger}}/api/traces/1" {:status 200
                                                                  :body   jaeger-response}})]

  (let [response (GET "/api/traces/1/sequence-diagram")]
    (match? {:status 200
             :body   {"sequence_diagram" {"start_time"      1500000000000
                                          "duration_ms"     1000
                                          "lifelines"       [{"name" "bff"}
                                                             {"name" "orders"}]
                                          "execution_boxes" [{"id"          "1"
                                                              "start_time"  1500000000000
                                                              "duration_ms" 1000
                                                              "lifeline"    "bff"}
                                                             {"id"          "3"
                                                              "start_time"  1500000000200
                                                              "duration_ms" 100
                                                              "lifeline"    "orders"}]
                                          "arrows"          [{"id"         "2"
                                                              "from"       "bff"
                                                              "to"         "orders"
                                                              "start_time" 1500000000100
                                                              "prefix"     "GET"
                                                              "label"      "/api/orders/1"}
                                                             {"id"         "3"
                                                              "from"       "orders"
                                                              "to"         "bff"
                                                              "start_time" 1500000000400
                                                              "label"      "response"}]}}}
            response)))
