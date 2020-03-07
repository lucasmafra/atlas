(ns atlas.controllers.trace-graph
  (:require [atlas.domain.trace-graph :as d-trace-graph]
            [common-clj.http-client.protocol :as hc-pro]))

(defn- fetch-trace [trace-id http-client]
  (-> http-client
      (hc-pro/request :jaeger/get-trace {:path-params {:id trace-id}})
      :body
      :data
      first))

(defn get-graph [trace-id {:keys [http-client]}]
  (let [trace (fetch-trace trace-id http-client)]
    (d-trace-graph/build-graph trace)))
