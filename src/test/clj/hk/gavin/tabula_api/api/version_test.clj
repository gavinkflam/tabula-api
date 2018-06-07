(ns hk.gavin.tabula-api.api.version-test
  (:require [clojure.test :refer :all]
            [hk.gavin.tabula-api.server :as server]
            [hk.gavin.tabula-api.test-util :as util]))

(deftest version-test
  (server/run-dev)
  (let [resp (util/request {:uri "/api/version"})]
    (is (= (get resp :status) 200))
    (is (= (get resp :body)
           (System/getProperty "hk.gavin.tabula-api.version")))))
