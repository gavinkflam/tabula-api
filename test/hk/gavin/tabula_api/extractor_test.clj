(ns hk.gavin.tabula-api.extractor-test
  (:require [clojure.test :refer :all]
            [hk.gavin.tabula-api.extractor :as extractor]))

(def sample-option-map
  {:area "%0,0,100,100" :format "CSV" :pages "all" :stream "yes"})

(def sample-option-map-expect
  ["--area" "%0,0,100,100" "--format" "CSV" "--pages" "all" "--stream"])

(defn option->string-list-assert
  [option expect]
  (is (= (extractor/option->string-list option) expect)))

(deftest string-arg-option->string-list-test
  (option->string-list-assert [:columns "10,20,30"] ["--columns" "10,20,30"]))

(deftest multi-string-arg-option->string-list-test
  (option->string-list-assert
   [:area ["%0,0,100,50", "%0,50,100,100"]]
   ["--area" "%0,0,100,50" "--area" "%0,50,100,100"]))

(deftest multi-string-arg-option->string-list-single-element-test
  (option->string-list-assert [:area ["%0,0,100,50"]] ["--area" "%0,0,100,50"]))

(deftest multi-string-arg-option->string-list-string-test
  (option->string-list-assert [:area "%0,0,100,50"] ["--area" "%0,0,100,50"]))

(deftest boolean-flag-option->string-list-test
  (option->string-list-assert [:guess "true"] ["--guess"]))

(deftest boolean-flag-option->string-list-false-string-test
  (option->string-list-assert [:guess "false"] []))

(deftest boolean-flag-option->string-list-empty-string-test
  (option->string-list-assert [:guess ""] ["--guess"]))

(deftest boolean-flag-option->string-list-any-string-test
  (option->string-list-assert [:guess "yolo"] ["--guess"]))

(deftest boolean-flag-option->string-list-no-string-test
  (option->string-list-assert [:guess "no"] []))

(deftest unsupported-option->string-list-no-string-test
  (option->string-list-assert [:poitroae "poitroaeitroae"] []))

(deftest option-map->string-list-test
  (is (=
       (extractor/option-map->string-list sample-option-map)
       sample-option-map-expect)))
