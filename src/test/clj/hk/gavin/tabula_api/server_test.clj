(ns hk.gavin.tabula-api.server-test
  (:require [clojure.core.async :as async]
            [clojure.test :refer :all]
            [hk.gavin.tabula-api.api.version-test :as version-test]
            [hk.gavin.tabula-api.server :as server]
            [hk.gavin.tabula-api.test-util :as util]))

(use-fixtures :each util/no-servers-running-fixture)

(deftest test-run-dev
  (server/run-dev)
  (version-test/test-api-version)
  (util/stop-dev-serv))

(deftest test-server-main
  (let [c (async/go (server/-main))]
    (async/alts!! [c (async/timeout 100)])
    (version-test/test-api-version)
    (util/stop-prod-serv)))
