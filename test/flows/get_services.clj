(ns flows.get-services
  (:require [common-clj.state-flow-helpers.config :as config]
            [common-clj.state-flow-helpers.http-client :as http-client]
            [common-clj.state-flow-helpers.http-server :as http-server]
            [flows.aux.init :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]))

(def mock-http-client
  {"http://jaeger.com/api/services" {:status 200 :body {:data ["orders" "feed"]}}})

(defflow get-services
  :pre-conditions [(config/assoc-in! [:known-hosts :jaeger] "http://jaeger.com")
                   (http-client/mock! mock-http-client)]

  [response (http-server/request-arrived! :get "/api/services")]

  (match? {:status 200 :body {"services" ["orders" "feed"]}}
          response))
