(ns hk.gavin.tabula-api.api.extract-test
  (:require [clojure.test :refer :all]
            [hk.gavin.tabula-api.server :as server]
            [hk.gavin.tabula-api.test-util :as util])
  (:import (org.apache.commons.io FileUtils)))

(def base-form
  [{:name "area"   :content "%0,0,100,50"}
   {:name "area"   :content "%0,50,100,100"}
   {:name "format" :content "CSV"}
   {:name "pages"  :content "1"}
   {:name "stream" :content "yes"}
   {:name "file"   :content (util/resource-file "multi-column.pdf")}])

(deftest extract-tables-test
  (server/run-dev)
  (let [resp (util/request {:uri "/api/extract"
                            :method :post
                            :multipart base-form})
        expect-csv (util/resource-file "multi-column.csv")
        output-csv (java.io.File/createTempFile "extract-tables-test" ".csv")]
    (is (= (get resp :status) 200))
    (FileUtils/writeStringToFile output-csv (get resp :body) "UTF-8")
    (is (FileUtils/contentEqualsIgnoreEOL expect-csv output-csv nil))))

(deftest extract-tables-format-error-test
  (server/run-dev)
  (let [form (util/update-field base-form "format" "FOO")
        resp (util/request {:uri "/api/extract"
                            :method :post
                            :multipart form})]
    (is (= (get resp :status) 400))
    (is (= (get resp :body)
           "format FOO is illegal. Available formats: CSV,TSV,JSON"))))

(deftest extract-tables-file-missing-error-test
  (server/run-dev)
  (let [form (util/update-field base-form "file" "")
        resp (util/request {:uri "/api/extract"
                            :method :post
                            :multipart form})]
    (is (= (get resp :status) 400))
    (is (= (get resp :body) "file is missing."))))

(deftest extract-tables-file-non-pdf-error-test
  (server/run-dev)
  (let [form (util/update-field
              base-form "file" (util/resource-file "multi-column.csv"))
        resp (util/request {:uri "/api/extract"
                            :method :post
                            :multipart form})]
    (is (= (get resp :status) 400))
    (is (= (get resp :body) "file is not a valid PDF file."))))
