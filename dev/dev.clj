(ns dev
  (:require [atlas.ports.http-client :refer [client-overrides client-routes]]
            [atlas.ports.http-server :refer [server-overrides server-routes]]
            [atlas.system :refer [system]]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [com.stuartsierra.component.repl :refer [reset set-init]]
            [common-clj.config.in-memory-config :as imc]
            [common-clj.http-client.http-client :as hc]
            [common-clj.http-client.interceptors.with-mock-calls :as i-hc-mock]
            [common-clj.http-server.http-server :as hs]))

(def config
  {:app-name    :atlas
   :http-port   9000
   :known-hosts {:jaeger "https://prod-jaeger.nubank.com.br"}})

(def mock-endpoints
  {#"https://prod-jaeger.nubank.com.br/api/traces/.*$"
   (fn [request] {:status 200
                  :body (-> request :path-params :id (str ".json") io/resource slurp)})})

(def dev-client-overrides
  (update client-overrides :extra-interceptors conj (i-hc-mock/with-mock-calls mock-endpoints)))

(def dev-system
  (merge system
         (component/system-map
          :config (imc/new-config config :dev)

          :http-client (component/using
                        (hc/new-http-client client-routes dev-client-overrides)
                        [:config])

          :http-server (component/using
                        (hs/new-http-server server-routes server-overrides)
                        [:config :http-client]))))

(set-init (constantly dev-system))

(def -main (partial component/start dev-system))

(comment
  (reset))
