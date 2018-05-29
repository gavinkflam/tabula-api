(ns hk.gavin.tabula-api.extractor-test
  (:require [clojure.test :refer :all]
            [hk.gavin.tabula-api.extractor :as extractor]))

(def sample-option-map
  {:area ["%0,0,100,50" "%0,50,100,100"] :format "CSV"
   :pages "1" :stream "yes"})

(def sample-option-map-expect
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

(deftest boolean-flag-option->string-vector-test
  (option->string-vector-assert [:guess "true"] ["--guess"]))

(deftest boolean-flag-option->string-vector-false-string-test
  (option->string-vector-assert [:guess "false"] []))

(deftest boolean-flag-option->string-vector-empty-string-test
  (option->string-vector-assert [:guess ""] ["--guess"]))

(deftest boolean-flag-option->string-vector-any-string-test
  (option->string-vector-assert [:guess "yolo"] ["--guess"]))

(deftest boolean-flag-option->string-vector-no-string-test
  (option->string-vector-assert [:guess "no"] []))

(deftest unsupported-option->string-vector-no-string-test
  (option->string-vector-assert [:poitroae "poitroaeitroae"] []))

(deftest option-map->string-vector-test
  (is (=
       (extractor/option-map->string-vector sample-option-map)
       sample-option-map-expect)))
