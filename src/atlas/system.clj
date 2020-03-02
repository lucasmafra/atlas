(ns atlas.system
  (:gen-class)
  (:require [atlas.ports.http-client :refer [client-overrides client-routes]]
            [atlas.ports.http-server :refer [server-overrides server-routes]]
            [com.stuartsierra.component :as component]
            [common-clj.config.edn-config :as edn-config]
            [common-clj.http-client.http-client :as hc]
            [common-clj.http-server.http-server :as hs]))

(def system
  (component/system-map
   :config            (edn-config/new-config)

   :http-client       (component/using
                       (hc/new-http-client client-routes client-overrides)
                       [:config])

   :http-server       (component/using
                       (hs/new-http-server server-routes server-overrides)
                       [:config :http-client])))

(def -main (partial component/start system))
