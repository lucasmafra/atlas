(ns flows.get-services
  (:require [common-clj.state-flow-helpers.http-client :as http-client]
            [common-clj.state-flow-helpers.http-server :refer [GET]]
            [flows.aux.init :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(def mock-http-calls
  {"{{jaeger}}/api/services" {:status 200 :body {:data ["orders" "feed"]}}})

(defflow get-services
  :pre-conditions [(http-client/mock! mock-http-calls)]

  [response (GET "/api/services")]

  (match? {:status 200 :body {"services" ["orders" "feed"]}}
          response))
