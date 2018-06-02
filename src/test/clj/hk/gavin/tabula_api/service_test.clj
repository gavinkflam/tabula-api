(ns hk.gavin.tabula-api.service-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [hk.gavin.tabula-api.server :as server])
  (:import (org.apache.commons.io FileUtils)))

(def base-req
  {:scheme :http
   :server-name "localhost"
   :server-port 8080})

(def sample-form
  [{:name "area"   :content "%0,0,100,50"}
   {:name "area"   :content "%0,50,100,100"}
   {:name "format" :content "CSV"}
   {:name "pages"  :content "1"}
   {:name "stream" :content "yes"}
   {:name "file"   :content (io/file (io/resource "multi-column.pdf"))}])

(defn request
  [req]
  (client/request (merge base-req req)))

(deftest extract-tables-test
  (server/run-dev)
  (let [resp (request {:uri "/extract_tables"
                       :method :post
                       :multipart sample-form})
        expect-csv (io/file (io/resource "multi-column.csv"))
        output-csv (java.io.File/createTempFile "extract-tables-test" ".csv")]
    (is (= (get resp :status) 200))
    (FileUtils/writeStringToFile output-csv (get resp :body) nil)
    (is (FileUtils/contentEqualsIgnoreEOL expect-csv output-csv nil))))
