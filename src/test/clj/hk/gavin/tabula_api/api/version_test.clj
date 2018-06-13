(ns hk.gavin.tabula-api.api.version-test
  "Tests for version API."
  (:require [clojure.test :refer :all]
            [hk.gavin.tabula-api.meta :as meta]
            [hk.gavin.tabula-api.test-util :as util]))

(use-fixtures :once util/dev-server-running-fixture)

(defn test-api-version
  "Test the version API and check the response."
  []
  (let [resp (util/request {:uri "/api/version"})]
    (is (= (get resp :status) 200))
    (is (= (get resp :body) @meta/version-string))))

(deftest version-test
  (test-api-version))
