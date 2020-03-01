(ns flows.get-operations
  (:require [common-clj.state-flow-helpers.http-client :as http-client]
            [common-clj.state-flow-helpers.http-server :refer [GET]]
            [flows.aux.init :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(def mock-http-calls
  {"{{jaeger}}/api/services/orders/operations"
   {:status 200 :body {:data ["get-orders" "place-order"]}}})

(defflow get-operations
  :pre-conditions [(http-client/mock! mock-http-calls)]

  [response (GET "/api/services/orders/operations")]

  (match? {:status 200 :body {"operations" ["get-orders" "place-order"]}}
          response))
