(ns atlas.schemata.jaeger
  (:require [common-clj.schema :as cs]
            [common-clj.schema-helpers :as csh]
            [schema.core :as s]))

(def JaegerGetServicesResponse
  (csh/loose-schema
   {:data [s/Str]}))

(def JaegerGetOperationsResponse
  (csh/loose-schema
   {:data [s/Str]}))

(def JaegerSearchTraceQuery
  {:service   s/Str
   :operation s/Str
   :limit     cs/PosInt
   :start     cs/TimestampMicroseconds
   :end       cs/TimestampMicroseconds})

(def Span
  {:trace-id       s/Str
   :span-id        s/Str
   :process-id     s/Str
   :operation-name s/Str
   :start-time     cs/TimestampMicroseconds
   :duration       s/Int})

(def TraceProcess
  {:service-name s/Str})

(def ProcessesMap
  {s/Keyword TraceProcess})

(def Trace
  {:trace-id  s/Str
   :spans     [Span]
   :processes ProcessesMap})

(def JaegerSearchTraceResponse
  (csh/loose-schema
   {:data [Trace]}))
