(defproject hk.gavin.tabula-api "0.1.0-SNAPSHOT"
  :description "An API server extracting tables from PDF files via tabula-java."
  :url "https://github.com/gavinkflam/tabula-api"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :key "mit"
            :year 2018}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [commons-cli/commons-cli "1.4"]
                 [io.pedestal/pedestal.service "0.5.3"]
                 [io.pedestal/pedestal.jetty "0.5.3"]
                 [org.apache.tika/tika-core "1.18"]
                 [org.slf4j/slf4j-simple "1.7.25"]
                 [technology.tabula/tabula "1.0.2"]]
  :source-paths ["src/main/clj"]
  :test-paths ["src/test/clj"]
  :resource-paths ["src/main/resources"]
  :profiles {:dev {:dependencies [[clj-http "3.9.0"]
                                  [commons-io/commons-io "2.6"]
                                  [io.pedestal/pedestal.service-tools "0.5.3"]]
                   :resource-paths ["src/test/resources"]}}
  :main hk.gavin.tabula-api.server
  :aot [hk.gavin.tabula-api.server])
