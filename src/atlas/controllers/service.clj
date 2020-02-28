(ns atlas.controllers.service
  (:require [common-clj.http-client.protocol :as hc-pro]))

(defn get-services [{:keys [http-client]}]
  (:data (:body (hc-pro/request http-client :jaeger/get-services))))
