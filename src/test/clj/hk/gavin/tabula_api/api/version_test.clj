(ns hk.gavin.tabula-api.api.version-test
  (:require [clojure.test :refer :all]
            [hk.gavin.tabula-api.meta :as meta]
            [hk.gavin.tabula-api.test-util :as util]))

(use-fixtures :once util/dev-server-running-fixture)

(defn test-api-version
  []
  (let [resp (util/request {:uri "/api/version"})]
    (is (= (get resp :status) 200))
    (is (= (get resp :body) @meta/version-string))))

(deftest test-version
  (test-api-version))
