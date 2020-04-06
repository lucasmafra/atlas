(ns dev
  (:require [atlas.system :refer [system]]
            [com.stuartsierra.component :as component]
            [com.stuartsierra.component.repl :refer [reset set-init start stop]]
            [common-clj.config.in-memory-config :as imc]))

(def config
  {:app-name    :atlas
   :http-port   9000
   :known-hosts {:jaeger "https://prod-jaeger.nubank.com.br"}})

(def dev-system
  (merge system
         (component/system-map
          :config (imc/new-config config :dev))))

(set-init (constantly dev-system))

(def -main (partial component/start dev-system))
