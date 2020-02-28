(ns dev
  (:require [com.stuartsierra.component :as component]
            [com.stuartsierra.component.repl :refer [set-init start stop reset]]
            [common-clj.components.config.in-memory-config :as imc]
            [atlas.system :refer [system]]
            [schema.core :as s]))

(def config
  {:app-name  :atlas
   :http-port 9000
   :known-hosts {:jaeger "DEV JAEGER URL"}})

(def dev-system
  (merge system
         (component/system-map
          :config (imc/new-config config :dev))))

(set-init (constantly dev-system))
