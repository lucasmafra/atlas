(ns atlas.ports.http-server
  (:require [atlas.controllers.mock-trace :as c-mock-trace]
            [atlas.controllers.operation :as c-operation]
            [atlas.controllers.sequence-diagram :as c-sequence-diagram]
            [atlas.controllers.service :as c-service]
            [atlas.controllers.trace-graph :as c-trace-graph]
            [atlas.controllers.trace-search :as c-trace-search]
            [atlas.schemata.operation :as s-operation]
            [atlas.schemata.sequence-diagram :as s-sequence-diagram]
            [atlas.schemata.service :as s-service]
            [atlas.schemata.trace-graph :as s-trace-graph]
            [atlas.schemata.trace-search :as s-trace-search]
            [cheshire.core :refer [generate-string]]
            [common-clj.coercion :as coercion :refer [int-matcher]]
            [common-clj.http-client.interceptors.json-deserializer :as deserializer]
            [common-clj.http-server.interceptors.helpers :refer [ok]]
            [schema.core :as s]))

(def server-routes
  {:route/hello
   {:path            "/"
    :method          :get
    :response-schema {:message s/Str}
    :handler         (constantly (ok {:message "I'm alive"}))}

   :route/get-services
   {:path            "/api/services"
    :method          :get
    :response-schema s-service/GetServicesResponse
    :handler         (fn [_ components]
                       (ok {:services (c-service/get-services components)}))}

   :route/get-operations
   {:path               "/api/services/:service/operations"
    :method             :get
    :path-params-schema {:service s/Str}
    :response-schema    s-operation/GetOperationsResponse
    :handler            (fn [{{:keys [service]} :path-params} components]
                          (ok {:operations (c-operation/get-operations service components)}))}

   :route/search-trace
   {:path                "/api/traces"
    :method              :get
    :query-params-schema s-trace-search/TraceSearchQuery
    :response-schema     s-trace-search/TraceSearchResponse
    :handler             (fn [{:keys [query-params]} components]
                           (ok {:traces (c-trace-search/search-trace query-params components)}))
    :overrides           {:query-params-coercer
                          {:extension {s-trace-search/TraceSearchLimit int-matcher}}}}

   :route/get-trace-graph
   {:path               "/api/traces/:id/graph"
    :method             :get
    :path-params-schema {:id s/Str}
    :response-schema    s-trace-graph/TraceGraphResponse
    :handler            (fn [{{:keys [id]} :path-params} components]
                          (ok {:graph (c-trace-graph/get-graph id components)}))}

   :route/get-sequence-diagram
   {:path               "/api/traces/:id/sequence-diagram"
    :method             :get
    :path-params-schema {:id s/Str}
    :response-schema    s-sequence-diagram/SequenceDiagramResponse
    :handler            (fn [{{:keys [id]} :path-params} components]
                          (ok {:sequence-diagram (c-sequence-diagram/get-sequence-diagram id components)}))}

   :route/mock-trace
   {:path            "/api/admin/mock-trace"
    :method          :post
    :request-schema  s/Any
    :response-schema {}
    :handler         (fn [{trace :body} components]
                       (c-mock-trace/mock-trace
                        (-> trace
                            generate-string
                            (deserializer/default-deserialize-fn
                             {"traceID"   :trace-id
                              "spanID"    :span-id
                              "processID" :process-id})))
                       (ok {}))}})

;; --- SERVER OVERRIDES ---
(def server-overrides {})
