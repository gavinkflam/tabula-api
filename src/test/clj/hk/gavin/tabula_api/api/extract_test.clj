(ns hk.gavin.tabula-api.api.extract-test
  (:require [clojure.test :refer :all]
            [hk.gavin.tabula-api.test-util :as util])
  (:import (org.apache.commons.io FileUtils)
           (java.io File)))

(use-fixtures :once util/dev-server-running-fixture)

(def base-form
  [{:name "area"   :content "%0,0,100,50"}
   {:name "area"   :content "%0,50,100,100"}
   {:name "pages"  :content "1"}
   {:name "stream" :content "yes"}
   {:name "file"   :content (util/resource-file "multi-column.pdf")}])

(defn test-extract-for
  [mime-type extension]
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

(deftest text-extract
  (testing "Extract as CSV"
    (test-extract-for "text/csv" ".csv"))
  (testing "Extract as TSV"
    (test-extract-for "text/tab-separated-values" ".tsv"))
  (testing "Extract as JSON"
    (test-extract-for "application/json" ".json")))

(deftest test-extract-errors
  (testing "Unsupported mime type"
    (let [resp (util/request {:uri "/api/extract"
                              :method :post
                              :accept "text/html"
                              :multipart base-form})]
      (is (= (get resp :status) 400))
      (is (= (get resp :body) "text/html is not supported."))))
  (testing "File not supplied"
    (let [form (util/update-field base-form "file" "")
          resp (util/request {:uri "/api/extract"
                              :method :post
                              :accept "text/csv"
                              :multipart form})]
      (is (= (get resp :status) 400))
      (is (= (get resp :body) "file is missing."))))
  (testing "Non-PDF file"
    (let [form (util/update-field
                base-form "file" (util/resource-file "multi-column.csv"))
          resp (util/request {:uri "/api/extract"
                              :method :post
                              :accept "text/csv"
                              :multipart form})]
      (is (= (get resp :status) 400))
      (is (= (get resp :body) "file is not a valid PDF file.")))))
