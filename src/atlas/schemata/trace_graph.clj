(ns atlas.schemata.trace-graph
  (:require [schema.core :as s]))

(def Node
  {:service s/Str})

(def Edge {:from s/Str
           :to   s/Str})

(def Graph
  {:nodes #{Node}
   :edges #{Edge}})

(def TraceGraphResponse
  {:graph Graph})
