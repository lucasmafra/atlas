(ns atlas.controllers.mock-trace
  (:require [atlas.schemata.jaeger :as s-jaeger]
            [cheshire.core :refer [generate-string]]
            [schema.core :as s])
  (:import [io.jaegertracing Configuration Configuration$SamplerConfiguration]))

(defn ->tracer [service-name]
  (-> Configuration
      (new service-name)
      (.withSampler (-> Configuration$SamplerConfiguration
                        new
                        (.withType "const")
                        (.withParam 1)))
      .getTracer))

(defn- add-tags [span-builder tags]
  (reduce
   (fn [span-builder {:keys [key value]}]
     (.withTag span-builder key value))
   span-builder
   tags))

(defn- start-span [tracer operation-name start-time references spans-map tags]
  (cond-> tracer
    true                      (.buildSpan operation-name)
    true                      (.withStartTimestamp start-time)
    (not (empty? references)) (.asChildOf (-> references first :span-id spans-map))
    (not (empty? tags))       (add-tags tags)
    true                      .start))

(defn- mock-span [span trace spans-map]
  (let [pid            (-> span :process-id)
        span-id        (-> span :span-id)
        processes      (-> trace :data first :processes)
        operation-name (-> span :operation-name)
        references     (-> span :references)
        tags           (-> span :tags)
        start-time     (-> span :start-time)
        duration       (-> span :duration)
        end-time       (+ start-time duration)
        service-name   (-> pid keyword processes :service-name)
        tracer         (->tracer service-name)
        started-span   (start-span tracer operation-name start-time references spans-map tags)
        scope          (-> tracer (.activateSpan started-span))]
      (.finish started-span end-time)
      (.close scope)
      (.close tracer)
      (assoc spans-map span-id started-span)))

(s/defn mock-trace [trace :- s-jaeger/TraceResponse]
  (let [spans (-> trace :data first :spans)]
    (reduce
     #(mock-span %2 trace %1)
     {}
     spans)))
