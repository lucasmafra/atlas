(ns atlas.ports.http-client
  (:require [atlas.schemata.jaeger :as s-jaeger]
            [schema.core :as s]))

(def client-routes
  {:jaeger/get-services
   {:host            "{{jaeger}}"
    :path            "/api/services"
    :method          :get
    :response-schema s-jaeger/JaegerGetServicesResponse}

   :jaeger/get-operations
   {:host               "{{jaeger}}"
    :path               "/api/services/:service/operations"
    :method             :get
    :path-params-schema {:service s/Str}
    :response-schema    s-jaeger/JaegerGetOperationsResponse}

   :jaeger/search-trace
   {:host                "{{jaeger}}"
    :path                "/api/traces"
    :method              :get
    :query-params-schema s-jaeger/JaegerSearchTraceQuery
    :response-schema     s/Any}})

;; --- CLIENT OVERRIDES ---
(def client-overrides
  {:extend-deserialization
   {"traceID"   :trace-id
    "spanID"    :span-id
    "processID" :process-id}})
