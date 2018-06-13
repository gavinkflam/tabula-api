(ns hk.gavin.tabula-api.server-test
  "Tests for server lifecycle functions."
  (:require [clojure.core.async :as async]
            [clojure.test :refer :all]
            [hk.gavin.tabula-api.api.version-test :as version-test]
            [hk.gavin.tabula-api.server :as server]
            [hk.gavin.tabula-api.test-util :as util]))

(use-fixtures :each util/no-servers-running-fixture)

(deftest run-dev-test
  (server/run-dev)
  (version-test/test-api-version)
  (util/stop-dev-serv))

(deftest server-main-test
  (let [c (async/go (server/-main))]
    (async/alts!! [c (async/timeout 100)])
    (version-test/test-api-version)
    (util/stop-prod-serv)))
