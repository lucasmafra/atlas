(ns atlas.controllers.trace-search
  (:require [atlas.adapters.trace-search :as a-trace-search]
            [atlas.domain.trace-search :as d-trace-search]
            [common-clj.http-client.protocol :as hc-pro]))

(defn- search-jaeger-trace [query-params http-client]
  (let [query-params {:query-params (a-trace-search/->jaeger-search-trace-query query-params)}]
    (:data (:body (hc-pro/request http-client :jaeger/search-trace query-params)))))

(defn- process-trace [{:keys [trace-id] :as trace}]
  (let [total-spans (d-trace-search/total-spans trace)
        duration-ms (d-trace-search/duration-ms trace)
        start-time  (d-trace-search/start-time trace)
        services    (d-trace-search/services-summary trace)]
    (a-trace-search/->trace-summary trace-id total-spans duration-ms start-time services)))

(defn search-trace [query-params {:keys [http-client]}]
  (let [traces (search-jaeger-trace query-params http-client)]
    (pmap process-trace traces)))
