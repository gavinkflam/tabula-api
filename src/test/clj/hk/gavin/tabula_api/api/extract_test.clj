(ns hk.gavin.tabula-api.api.extract-test
  (:require [clojure.test :refer :all]
            [hk.gavin.tabula-api.server :as server]
            [hk.gavin.tabula-api.test-util :as util])
  (:import (org.apache.commons.io FileUtils)
           (java.io File)))

(def base-form
  [{:name "area"   :content "%0,0,100,50"}
   {:name "area"   :content "%0,50,100,100"}
   {:name "pages"  :content "1"}
   {:name "stream" :content "yes"}
   {:name "file"   :content (util/resource-file "multi-column.pdf")}])

(defn test-extract-for
  [mime-type extension]
  (server/run-dev)
  (let [resp (util/request {:uri "/api/extract"
                            :method :post
                            :accept mime-type
                            :multipart base-form})
        expect-file (util/resource-file (str "multi-column" extension))
        output-file (File/createTempFile "extract-tables-test" extension)]
    (is (= (get resp :status) 200))
    (is (= (get-in resp [:headers :content-type]) mime-type))
    (FileUtils/writeStringToFile output-file (get resp :body) "UTF-8")
    (is (FileUtils/contentEqualsIgnoreEOL expect-file output-file nil))))

(deftest extract-tables-csv-test
  (test-extract-for "text/csv" ".csv"))

(deftest extract-tables-tsv-test
  (test-extract-for "text/tab-separated-values" ".tsv"))

(deftest extract-tables-json-test
  (test-extract-for "application/json" ".json"))

(deftest extract-tables-format-error-test
  (server/run-dev)
  (let [resp (util/request {:uri "/api/extract"
                            :method :post
                            :accept "text/html"
                            :multipart base-form})]
    (is (= (get resp :status) 400))
    (is (= (get resp :body) "text/html is not supported."))))

(deftest extract-tables-file-missing-error-test
  (server/run-dev)
  (let [form (util/update-field base-form "file" "")
        resp (util/request {:uri "/api/extract"
                            :method :post
                            :accept "text/csv"
                            :multipart form})]
    (is (= (get resp :status) 400))
    (is (= (get resp :body) "file is missing."))))

(deftest extract-tables-file-non-pdf-error-test
  (server/run-dev)
  (let [form (util/update-field
              base-form "file" (util/resource-file "multi-column.csv"))
        resp (util/request {:uri "/api/extract"
                            :method :post
                            :accept "text/csv"
                            :multipart form})]
    (is (= (get resp :status) 400))
    (is (= (get resp :body) "file is not a valid PDF file."))))
