(ns flows.hello
  (:require [common-clj.state-flow-helpers.http-server :refer [GET]]
            [flows.aux.init :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(defflow hello
  [response (GET "/")]

  (match? {:status 200} response))
