(defproject atlas "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clojure.java-time "0.3.2"]
                 [lucasmafra/common-clj "1.0.7"]
                 [com.stuartsierra/component "0.4.0"]
                 [prismatic/schema "1.1.11"]]
  :main ^:skip-aot atlas.system
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:dependencies [[nubank/matcher-combinators "1.0.0"]
                                      [nubank/state-flow "2.2.4"]
                                      [org.clojure/tools.namespace "0.2.11"]
                                      [com.stuartsierra/component.repl "0.2.0"]]
                       :source-paths ["dev"]
                       :aliases {"lint-fix" ["do" "nsorg" "--replace," "kibit" "--replace"]}
                       :plugins [[jonase/eastwood "0.3.8"]]
                       :eastwood {:exclude-linters [:suspicious-test]}}})
