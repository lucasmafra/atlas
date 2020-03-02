(ns atlas.schemata.trace-search
  (:require [common-clj.schema.core :as cs]
            [schema.core :as s]))

(def TraceSearchLimit
  (s/pred (every-pred pos-int? #(<= % 500)) 'int-between-1-and-500))

(def TraceSearchQuery
  {:service   s/Str
   :operation s/Str
   :limit     TraceSearchLimit
   :start     cs/EpochMillis
   :end       cs/EpochMillis})

(def ServiceSummary
  {:name            s/Str
   :number-of-spans s/Int})

(def TraceSummary
  {:services    [ServiceSummary]
   :total-spans s/Int
   :trace-id    s/Str
   :duration-ms s/Int
   :start-time  cs/EpochMillis})

(def TraceSearchResponse
  {:traces [TraceSummary]})
