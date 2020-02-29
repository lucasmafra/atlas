(ns atlas.controllers.operation
  (:require [common-clj.http-client.protocol :as hc-pro]))

(defn get-operations [service {:keys [http-client]}]

  (let [path-params {:path-params {:service service}}
        a 1]
    (:data (:body (hc-pro/request http-client :jaeger/get-operations path-params)))))
