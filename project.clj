(defproject atlas "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clojure.java-time "0.3.2"]
                 [lucasmafra/common-clj "1.5.0"]
                 [com.stuartsierra/component "0.4.0"]
                 [prismatic/schema "1.1.11"]]

  :resource-paths ["config", "resources"]
  :main ^:skip-aot atlas.system
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:dependencies [[nubank/matcher-combinators "1.0.0"]
                                      [nubank/state-flow "2.2.4"]
                                      [org.clojure/tools.namespace "0.2.11"]
                                      [com.stuartsierra/component.repl "0.2.0"]
                                      [clj-kondo "2020.02.28-1"]]
                       :source-paths ["dev"]
                       :plugins [[lucasmafra/lein-cljfmt "0.5.5"]
                                 [lein-kibit "0.1.8"]
                                 [lein-nsorg "0.3.0"]
                                 [clj-kondo "2020.02.28-1"]]
                       :aliases {"lint-fix" ["do" "nsorg" "--replace," "kibit" "--replace," "cljfmt" "fix"]
                                 "clj-kondo" ["run" "-m" "clj-kondo.main"]}}})
