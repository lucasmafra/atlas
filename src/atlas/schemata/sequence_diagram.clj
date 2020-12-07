(ns atlas.schemata.sequence-diagram
  (:require [common-clj.schema.core :as cs]
            [schema.core :as s]))

(def Lifeline
  {:id    s/Str
   :label s/Str
   :kind  (s/enum :service :topic)
   :time  cs/EpochMillis})

(def ExecutionBox
  {:id          s/Str
   :start-time  cs/EpochMillis
   :duration-ms s/Num
   :lifeline    s/Str})

(def Node
  {:id       s/Str
   :time     cs/EpochMillis
   :meta     {s/Keyword s/Str}
   :lifeline s/Str})

(def Arrow
  {:id                      s/Str
   :from                    s/Str
   :to                      s/Str
   :start-time              cs/EpochMillis
   (s/optional-key :prefix) s/Str
   :label                   s/Str})

(def SequenceDiagram
  {:start-time      cs/EpochMillis
   :duration-ms     cs/PosInt
   :lifelines       [Lifeline]
   :execution-boxes [ExecutionBox]
   :nodes           [Node]
   :arrows          [Arrow]})

(def SequenceDiagramResponse
  {:sequence-diagram SequenceDiagram})
