(ns atlas.ports.http-server
  (:require [atlas.controllers.operation :as c-operation]
            [atlas.controllers.service :as c-service]
            [atlas.controllers.trace-search :as c-trace-search]
            [atlas.schemata.operation :as s-operation]
            [atlas.schemata.service :as s-service]
            [atlas.schemata.trace-search :as s-trace-search]
            [common-clj.coercion :refer [int-matcher]]
            [common-clj.http-server.interceptors.helpers :refer [ok]]
            [schema.core :as s]))

(def server-routes
  {:route/get-services
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
                          {:extension {s-trace-search/TraceSearchLimit int-matcher}}}}})

;; --- SERVER OVERRIDES ---
(def server-overrides {})

