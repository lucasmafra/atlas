(ns atlas.domain.trace-search
  (:require [atlas.schemata.jaeger :as s-jaeger]
            [atlas.schemata.trace-search :as s-trace-search]
            [common-clj.schema.core :as cs]
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

(defn- safe-inc [value]
  (if (nil? value)
    1
    (inc value)))

(s/defn services-summary :- [s-trace-search/ServiceSummary]
  [{:keys [spans processes]} :- s-jaeger/Trace]
  (->> spans
       (reduce (fn [acc {:keys [process-id]}]
                 (let [{:keys [service-name]} (processes (keyword process-id))]
                   (update acc service-name safe-inc)))
               {})
       (map (fn [[name number-of-spans]]
              {:name            name
               :number-of-spans number-of-spans}))))
