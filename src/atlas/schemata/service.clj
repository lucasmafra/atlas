(ns atlas.schemata.service
  (:require [schema.core :as s]))

(def GetServicesResponse
  {:services [s/Str]})
