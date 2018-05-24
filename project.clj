(defproject hk.gavin.tabula-api "0.1.0-SNAPSHOT"
  :description "An API server extracting tables from PDF files via tabula-java."
  :url "https://github.com/gavinkflam/tabula-api"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :key "mit"
            :year 2018}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [io.pedestal/pedestal.service "0.5.3"]
                 [io.pedestal/pedestal.jetty "0.5.3"]
                 [org.slf4j/slf4j-simple "1.7.25"]]
  :profiles {:dev {:dependencies [[io.pedestal/pedestal.service-tools "0.5.3"]]}}
  :main hk.gavin.tabula-api.server)
