(ns flows.search-trace
  (:require [common-clj.json :as json]
            [common-clj.state-flow-helpers.config :as config]
            [common-clj.state-flow-helpers.http-client :as http-client]
            [common-clj.state-flow-helpers.http-server :as http-server]
            [flows.aux.init :refer [defflow]]
            [schema.core :as s]
            [schema.spec.core :as spec]
            [schema.utils :as utils]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [common-clj.schema :as cs]))

(def jaeger-request
  (str "http://jaeger.com/api/traces?"
       "service=orders&"
       "operation=place-order&"
       "limit=10&"
       "start=1581797873000000&"
       "end=1582769681000000"))

(def jaeger-response
  {"data" [{"traceID"   "trace-1"
            "spans"     [{"traceID"       "trace-1"
                          "spanID"        "span-1-1"
                          "processID"     "p1"
                          "operationName" "op-1-1"
                          "startTime"     1500000000000000
                          "duration"      1000}
                         {"traceID"       "trace-1"
                          "spanID"        "span-1-2"
                          "processID"     "p1"
                          "operationName" "op-1-2"
                          "startTime"     1499999999999999
                          "duration"      1001}]
            "processes" {"p1" {"serviceName" "orders"}}}
           
           {"traceID"   "trace-2"
            "spans"     [{"traceID"        "trace-2"
                          "spanID"         "span-2-1"
                          "processID"      "p1"
                          "operation-name" "op-2-1"
                          "start-time"     1500000000000002
                          "duration"       1002}
                         {"traceID"        "trace-2"
                          "spanID"         "span-2-2"
                          "processID"      "p2"
                          "operationName" "op-2-2"
                          "start-time"     1500000000000003
                          "duration"       1003}]
            "processes" {"p1" {"serviceName" "feed"}
                         "p2" {"serviceName" "orders"}}}]})

(defflow search-trace
  :pre-conditions [(config/assoc-in! [:known-hosts :jaeger] "http://jaeger.com")
                   (http-client/mock! {jaeger-request {:status 200 :body jaeger-response}})]

  [response (http-server/request-arrived! :get (str "/api/traces?"
                                                    "service=orders&"
                                                    "operation=place-order&"
                                                    "limit=10&"
                                                    "start=1581797873000&"
                                                    "end=1582769681000"))]
  
  (match? {:status 200
           :body {"traces" [{"trace_id"    "trace-1"
                             "total_spans" 2
                             "duration_ms" 2001
                             "start_time"  1499999999999
                             "services"    [{"name"            "orders"
                                             "number_of_spans" 2}]}
                                        
                            {"trace_id"    "trace-2"
                             "total_spans" 2
                             "duration_ms" 2005
                             "start_time"  1500000000000
                             "services"    [{"name"            "feed"
                                             "number_of_spans" 1}
                                            {"name"            "orders"
                                             "number_of_spans" 1}]}]}}
          response))
