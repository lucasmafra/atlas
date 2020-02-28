(ns atlas.domain.trace-search
  (:require [atlas.schemata.jaeger :as s-jaeger]
            [atlas.schemata.trace-search :as s-trace-search]
            [common-clj.schema :as cs]
            [java-time :refer [instant]]
            [schema.core :as s]))

(s/defn total-spans :- s/Int
  [{:keys [spans]} :- s-jaeger/Trace]
  (count spans))

(s/defn duration-ms :- s/Int
  [{:keys [spans]} :- s-jaeger/Trace]
  (let [durations (map :duration spans)]
    (reduce + durations)))

(s/defn start-time :- cs/EpochMillis
  [{:keys [spans]} :- s-jaeger/Trace]
  (let [start-times (map :start-time spans)
        oldest      (first (sort start-times))]
    (instant (/ oldest 1000))))

(s/defn services-summary :- [s-trace-search/ServiceSummary]
  [{:keys [spans processes]} :- s-jaeger/Trace]
  (map (fn [[pid {:keys [service-name]}]]
         {:name            service-name
          :number-of-spans (->> spans (filter #(= pid (keyword (:process-id %)))) count)})
       processes))
