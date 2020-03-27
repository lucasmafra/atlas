(ns atlas.schemata.sequence-diagram
  (:require [common-clj.schema.core :as cs]
            [schema.core :as s]))

(def Lifeline
  {:name s/Str})

(def ExecutionBox
  {:id          s/Str
   :start-time  cs/EpochMillis
   :duration-ms cs/PosInt
   :lifeline    s/Str})

(def Arrow
  {:id         s/Str
   :from       s/Str
   :to         s/Str
   :start-time cs/EpochMillis})

(def SequenceDiagram
  {:start-time      cs/EpochMillis
   :duration-ms     cs/PosInt
   :lifelines       [Lifeline]
   :execution-boxes [ExecutionBox]
   :arrows          [Arrow]})

(def SequenceDiagramResponse
  {:sequence-diagram SequenceDiagram})
