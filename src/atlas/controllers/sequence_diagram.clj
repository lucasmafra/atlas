(ns atlas.controllers.sequence-diagram
  (:require [atlas.domain.sequence-diagram :as d-sequence-diagram]
            [common-clj.http-client.protocol :as hc-pro]))

(defn- fetch-trace [trace-id http-client]
  (-> http-client
      (hc-pro/request :jaeger/get-trace {:path-params {:id trace-id}})
      :body
      :data
      first))

(defn get-sequence-diagram [trace-id {:keys [http-client]}]
  (let [trace (fetch-trace trace-id http-client)]
    {:start-time      (d-sequence-diagram/start-time trace)
     :duration-ms     (d-sequence-diagram/duration-ms trace)
     :lifelines       (d-sequence-diagram/lifelines trace)
     :execution-boxes (d-sequence-diagram/execution-boxes trace)
     :nodes           (d-sequence-diagram/nodes trace)
     :arrows          []}))
