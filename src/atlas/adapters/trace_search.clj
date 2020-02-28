(ns atlas.adapters.trace-search
  (:require [atlas.schemata.jaeger :as s-jaeger]
            [atlas.schemata.trace-search :as s-trace-search]
            [common-clj.schema :as cs]
            [schema.core :as s]))

(s/defn ->jaeger-search-trace-query :- s-jaeger/JaegerSearchTraceQuery
  [query :- s-trace-search/TraceSearchQuery]
  {:service   (:service query)
   :operation (:operation query)
   :limit     (:limit query)
   :start     (-> query :start .toEpochMilli (* 1000))
   :end       (-> query :end .toEpochMilli (* 1000))})

(s/defn ->trace-summary :- s-trace-search/TraceSummary
  [trace-id :- s/Str
   total-spans :- s/Int
   duration-ms :- s/Int
   start-time :- cs/EpochMillis
   services :- [s-trace-search/ServiceSummary]]
  {:trace-id    trace-id
   :total-spans total-spans
   :duration-ms duration-ms
   :start-time  start-time
   :services    services})
