(ns hk.gavin.tabula-api.api.extract-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [hk.gavin.tabula-api.server :as server])
  (:import (org.apache.commons.io FileUtils)))

(defn resource-file
  [file-name]
  (-> file-name io/resource io/file))

(def base-req
  {:scheme :http
   :server-name "localhost"
   :server-port 8080
   :throw-exceptions false})

(def base-form
  [{:name "area"   :content "%0,0,100,50"}
   {:name "area"   :content "%0,50,100,100"}
   {:name "format" :content "CSV"}
   {:name "pages"  :content "1"}
   {:name "stream" :content "yes"}
   {:name "file"   :content (resource-file "multi-column.pdf")}])

(defn request
  [req]
  (client/request (merge base-req req)))

(defn update-field
  [form name content]
  (map #(if (= (get % :name) name) (assoc % :content content) %) form))

(deftest extract-tables-test
  (server/run-dev)
  (let [resp (request {:uri "/api/extract"
                       :method :post
                       :multipart base-form})
        expect-csv (io/file (io/resource "multi-column.csv"))
        output-csv (java.io.File/createTempFile "extract-tables-test" ".csv")]
    (is (= (get resp :status) 200))
    (FileUtils/writeStringToFile output-csv (get resp :body) "UTF-8")
    (is (FileUtils/contentEqualsIgnoreEOL expect-csv output-csv nil))))

(deftest extract-tables-format-error-test
  (server/run-dev)
  (let [resp (request {:uri "/api/extract"
                       :method :post
                       :multipart (update-field base-form "format" "FOO")})]
    (is (= (get resp :status) 400))
    (is (= (get resp :body)
           "format FOO is illegal. Available formats: CSV,TSV,JSON"))))

(deftest extract-tables-file-missing-error-test
  (server/run-dev)
  (let [resp (request {:uri "/api/extract"
                       :method :post
                       :multipart (update-field base-form "file" "")})]
    (is (= (get resp :status) 400))
    (is (= (get resp :body) "file is missing."))))

(deftest extract-tables-file-non-pdf-error-test
  (server/run-dev)
  (let [form (update-field base-form "file" (resource-file "multi-column.csv"))
        resp (request {:uri "/api/extract"
                       :method :post
                       :multipart form})]
    (is (= (get resp :status) 400))
    (is (= (get resp :body) "file is not a valid PDF file."))))
