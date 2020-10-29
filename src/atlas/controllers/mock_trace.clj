(ns atlas.controllers.mock-trace
  (:require [atlas.schemata.jaeger :as s-jaeger]
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
    (seq references) (.asChildOf (-> references first :span-id spans-map))
    (seq tags)       (add-tags tags)
    true                      .start))

(defn- mock-span [span trace spans-map]
  (let [pid            (:process-id span)
        span-id        (:span-id span)
        processes      (-> trace :data first :processes)
        operation-name (:operation-name span)
        references     (:references span)
        tags           (:tags span)
        start-time     (:start-time span)
        duration       (:duration span)
        end-time       (+ start-time duration)
        service-name   (-> pid keyword processes :service-name)
        tracer         (->tracer service-name)
        started-span   (start-span tracer operation-name start-time references spans-map tags)
        scope          (.activateSpan tracer started-span)]
    (.finish started-span end-time)
    (.close scope)
    (.close tracer)
    (assoc spans-map span-id started-span)))

(s/defn mock-trace [trace :- s-jaeger/TraceResponse]
  (let [spans (-> trace :data first :spans)]
    (reduce #(mock-span %2 trace %1) {} spans)))
