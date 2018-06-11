(ns hk.gavin.tabula-api.extractor-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [hk.gavin.tabula-api.extractor :as extractor]
            [hk.gavin.tabula-api.test-util :as util])
  (:import (org.apache.commons.io FileUtils)))

(def sample-option-map
  {:area ["%0,0,100,50" "%0,50,100,100"] :format "CSV"
   :pages "1" :stream "true"})

(def sample-option-map-expect-string-vector
  ["--area" "%0,0,100,50" "--area" "%0,50,100,100" "--format" "CSV"
   "--pages" "1" "--stream"])

(defn option->string-vector-assert
  [option expect]
  (is (= (extractor/option->string-vector option) expect)))

(deftest string-arg-option->string-vector-test
  (option->string-vector-assert
   [:columns "10,20,30"] ["--columns" "10,20,30"]))

(deftest multi-string-arg-option->string-vector-test
  (option->string-vector-assert
   [:area ["%0,0,100,50" "%0,50,100,100"]]
   ["--area" "%0,0,100,50" "--area" "%0,50,100,100"]))

(deftest multi-string-arg-option->string-vector-single-element-test
  (option->string-vector-assert
   [:area ["%0,0,100,50"]] ["--area" "%0,0,100,50"]))

(deftest multi-string-arg-option->string-vector-string-test
  (option->string-vector-assert
   [:area "%0,0,100,50"] ["--area" "%0,0,100,50"]))

(deftest truthy-boolean-flag-values-test
  (option->string-vector-assert [:guess true] ["--guess"])
  (option->string-vector-assert [:guess "true"] ["--guess"])
  (option->string-vector-assert [:guess "True"] ["--guess"]))

(deftest falsy-boolean-flag-values-test
  (option->string-vector-assert [:guess false] [])
  (option->string-vector-assert [:guess "false"] [])
  (option->string-vector-assert [:guess "False"] []))

(deftest invalid-boolean-flag-values-test
  (option->string-vector-assert [:guess nil] [])
  (option->string-vector-assert [:guess ""] [])
  (option->string-vector-assert [:guess "yolo"] [])
  (option->string-vector-assert [:guess "TrUe"] []))

(deftest option-map->string-vector-test
  (is (=
       (extractor/option-map->string-vector sample-option-map)
       sample-option-map-expect-string-vector)))

(deftest option-map->command-line-test
  (let [cmd-line (extractor/option-map->command-line sample-option-map)]
    (is (.hasOption cmd-line "area"))
    (let [areas (.getOptionValues cmd-line "area")]
      (is (= (first areas) "%0,0,100,50"))
      (is (= (second areas) "%0,50,100,100")))
    (is (.hasOption cmd-line "format"))
    (is (= (.getOptionValue cmd-line "format") "CSV"))
    (is (.hasOption cmd-line "pages"))
    (is (= (.getOptionValue cmd-line "pages") "1"))
    (is (.hasOption cmd-line "stream"))
    (is (not (.hasOption cmd-line "guess")))))

(deftest validate-pdf-file-pdf-file-test
  (let [multi-column-pdf (util/resource-file "multi-column.pdf")]
    (is (extractor/validate-pdf-file multi-column-pdf))))

(deftest validate-pdf-file-missing-file-test
  (is (thrown-with-msg?
       IllegalArgumentException #"file is missing."
       (extractor/validate-pdf-file nil))))

(deftest validate-pdf-file-non-pdf-file-test
  (let [multi-column-csv (util/resource-file "multi-column.csv")]
    (is (thrown-with-msg?
         IllegalArgumentException #"file is not a valid PDF file."
         (extractor/validate-pdf-file multi-column-csv)))))

(deftest extract-tables-test
  (let [multi-column-pdf (util/resource-file "multi-column.pdf")
        multi-column-csv (util/resource-file "multi-column.csv")
        output-csv (java.io.File/createTempFile "extract-tables-test" ".csv")]
    (extractor/extract-tables sample-option-map multi-column-pdf output-csv)
    (is (FileUtils/contentEqualsIgnoreEOL multi-column-csv output-csv nil))))
