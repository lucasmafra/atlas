(ns atlas.controllers.sequence-diagram
  (:require [atlas.domain.sequence-diagram :as d-sequence-diagram]
            [clj-http.fake :refer [with-fake-routes]]
            [common-clj.http-client.protocol :as hc-pro]
            [clojure.java.io :as io]))

(defn- fetch-trace [trace-id http-client]
  (-> http-client
      (hc-pro/request :jaeger/get-trace {:path-params {:id trace-id}})
      :body
      :data
      first))

(defn- with-mock-trace [trace-id http-client]
  (let [trace (-> trace-id (str ".json") io/resource slurp)]
    (with-fake-routes {#".*" (constantly {:status 200 :body trace})}
      (fetch-trace trace-id http-client))))

(defn get-sequence-diagram [trace-id {:keys [http-client]}]
  (let [trace (with-mock-trace trace-id http-client)]
    {:start-time      (d-sequence-diagram/start-time trace)
     :duration-ms     (d-sequence-diagram/duration-ms trace)
     :lifelines       (d-sequence-diagram/lifelines trace)
     :execution-boxes (d-sequence-diagram/execution-boxes trace)
     :arrows          (d-sequence-diagram/arrows trace)}))
