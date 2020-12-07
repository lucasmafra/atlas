(ns atlas.schemata.jaeger
  (:require [common-clj.schema.core :as cs]
            [common-clj.schema.helpers :as csh]
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

(def SpanReferenceType (s/enum :child-of :follows-from))

(def SpanReference
  {:ref-type SpanReferenceType
   :trace-id s/Str
   :span-id  s/Str})

(def SpanTag
  {:key   s/Str
   :type  s/Str
   :value s/Any})

(def LogField
  {:key   s/Str
   :type  s/Str
   :value s/Str})

(def SpanLog
  {:timestamp cs/TimestampMicroseconds
   :fields [LogField]})

(def Span
  {:trace-id              s/Str
   :span-id               s/Str
   :process-id            s/Keyword
   :operation-name        s/Str
   :start-time            cs/TimestampMicroseconds
   :duration              s/Int
   :references            [SpanReference]
   :tags                  [SpanTag]
   (s/optional-key :logs) [SpanLog]})

(def TraceProcess
  {:service-name s/Str})

(def ProcessesMap
  {s/Keyword TraceProcess})

(def Trace
  {:trace-id  s/Str
   :spans     [Span]
   :processes ProcessesMap})

(def TraceResponse
  (csh/loose-schema
   {:data [Trace]}))

(def JaegerSearchTraceResponse
  (csh/loose-schema
   {:data [Trace]}))
