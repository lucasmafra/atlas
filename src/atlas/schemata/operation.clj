(ns atlas.schemata.operation
  (:require [schema.core :as s]))

(def GetOperationsResponse
  {:operations [s/Str]})
