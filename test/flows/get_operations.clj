(ns flows.get-operations
  (:require [common-clj.state-flow-helpers.config :as config]
            [common-clj.state-flow-helpers.http-client :as http-client]
            [common-clj.state-flow-helpers.http-server :as http-server]
            [flows.aux.init :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(def mock-http-client
  {"http://jaeger.com/api/services/orders/operations"
   {:status 200 :body {:data ["get-orders" "place-order"]}}})

(defflow get-operations
  :pre-conditions [(config/assoc-in! [:known-hosts :jaeger] "http://jaeger.com")
                   (http-client/mock! mock-http-client)]

  [response (http-server/request-arrived! :get "/api/services/orders/operations")]

  (match? {:status 200 :body {"operations" ["get-orders" "place-order"]}}
          response))
