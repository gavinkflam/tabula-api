(ns hk.gavin.tabula-api.extractor-test
  "Tests for the extractor core."
  (:require [clojure.test :refer :all]
            [hk.gavin.tabula-api.extractor :as extractor]
            [hk.gavin.tabula-api.test-util :as util])
  (:import (java.io File)
           (org.apache.commons.io FileUtils)))

(def sample-option-map
  "A sample option map for extracting tables from multi-column.pdf."
  {:area ["%0,0,100,50" "%0,50,100,100"] :format "CSV"
   :pages "1" :stream "true"})

(def sample-option-map-expect-string-vector
  "The expected string vector derived from the corresponding sample option map."
  ["--area" "%0,0,100,50" "--area" "%0,50,100,100" "--format" "CSV"
   "--pages" "1" "--stream"])

(defn test-option->string-vector
  "Test deriving string vector from option map."
  [option expect]
  (is (= (#'extractor/option->string-vector option) expect)))

(deftest string-arg-test
  (testing "Valid string arg"
    (test-option->string-vector [:columns "10,20,30"] ["--columns" "10,20,30"]))
  (testing "Invalid string arg, should discard the option"
    (test-option->string-vector [:columns ["10,20,30", "40,50,60"]] [])))

(deftest multi-string-arg-test
  (testing "Single valid string value"
    (test-option->string-vector [:area "%0,0,100,50"] ["--area" "%0,0,100,50"]))
  (testing "Single invalid string value, should discard the option"
    (test-option->string-vector [:area true] []))
  (testing "Vector with one string element"
    (test-option->string-vector
     [:area ["%0,0,100,50"]] ["--area" "%0,0,100,50"]))
  (testing "Vector with multiple string element"
    (test-option->string-vector
     [:area ["%0,0,100,50" "%0,50,100,100"]]
     ["--area" "%0,0,100,50" "--area" "%0,50,100,100"]))
  (testing "Vector with multiple string element and invalid elements"
    (test-option->string-vector
     [:area ["%0,0,100,50" true "%0,50,100,100" false]]
     ["--area" "%0,0,100,50" "--area" "%0,50,100,100"])))

(deftest boolean-flag-arg-test
  (testing "Truthy values, should include the option"
    (test-option->string-vector [:guess true] ["--guess"])
    (test-option->string-vector [:guess "true"] ["--guess"])
    (test-option->string-vector [:guess "True"] ["--guess"]))
  (testing "Falsy values, should discard the option"
    (test-option->string-vector [:guess false] [])
    (test-option->string-vector [:guess "false"] [])
    (test-option->string-vector [:guess "False"] []))
  (testing "Invalid values, should discard the option"
    (test-option->string-vector [:guess nil] [])
    (test-option->string-vector [:guess ""] [])
    (test-option->string-vector [:guess "yolo"] [])
    (test-option->string-vector [:guess "TrUe"] [])))

(deftest unsupported-options-test
  (testing "Unsupported options, should discard the option"
    (test-option->string-vector [:how-can-you-code-without-lisp? nil] [])
    (test-option->string-vector [:lisp-the-best-of-all "true"] [])
    (test-option->string-vector [:yagni false] [])
    (test-option->string-vector [:choose-one ["red" "blue"]] [])))

(deftest option-map->string-vector-test
  (is (=
       (#'extractor/option-map->string-vector sample-option-map)
       sample-option-map-expect-string-vector)))

(deftest option-map->command-line-test
  (let [cmd-line (#'extractor/option-map->command-line sample-option-map)]
    (testing "Multi-string arg"
      (is (.hasOption cmd-line "area"))
      (let [areas (.getOptionValues cmd-line "area")]
        (is (= (first areas) "%0,0,100,50"))
        (is (= (second areas) "%0,50,100,100"))))
    (testing "String arg"
      (is (.hasOption cmd-line "format"))
      (is (= (.getOptionValue cmd-line "format") "CSV"))
      (is (.hasOption cmd-line "pages"))
      (is (= (.getOptionValue cmd-line "pages") "1")))
    (testing "Boolean-flag arg"
      (is (.hasOption cmd-line "stream"))
      (is (not (.hasOption cmd-line "guess"))))))

(deftest validate-pdf-file-test
  (let [multi-column-pdf (util/resource-file "multi-column.pdf")
        multi-column-csv (util/resource-file "multi-column.csv")]
    (testing "PDF file should pass"
      (is (extractor/validate-pdf-file multi-column-pdf)))
    (testing "Non-PDF file should fail"
      (is (thrown-with-msg?
           IllegalArgumentException #"file is not a valid PDF file."
           (extractor/validate-pdf-file multi-column-csv))))
    (testing "Missing file should fail"
      (is (thrown-with-msg?
           IllegalArgumentException #"file is missing."
           (extractor/validate-pdf-file nil))))))

(deftest extract-into-test
  (let [multi-column-pdf (util/resource-file "multi-column.pdf")
        multi-column-csv (util/resource-file "multi-column.csv")
        output-csv (File/createTempFile "extract-into-test" ".csv")]
    (extractor/extract-into sample-option-map multi-column-pdf output-csv)
    (is (FileUtils/contentEqualsIgnoreEOL multi-column-csv output-csv nil))))
