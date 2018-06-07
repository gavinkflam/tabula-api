(ns hk.gavin.tabula-api.server-test
  (:require [clojure.core.async :as async]
            [clojure.test :refer :all]
            [io.pedestal.test :refer :all]
            [io.pedestal.http :as http]
            [hk.gavin.tabula-api.server :as server]
            [hk.gavin.tabula-api.api.version-test :as version-test]))

(defn stop-all-servers
  []
  (http/stop @server/dev-serv)
  (http/stop @server/prod-serv))

(deftest run-prod-test
  (stop-all-servers)
  (let [c (async/go (server/run-prod))]
    (async/alts!! [c (async/timeout 100)])
    (version-test/test-api-version)
    (http/stop @server/prod-serv)))

(deftest run-dev-test
  (stop-all-servers)
  (server/run-dev)
  (version-test/test-api-version)
  (http/stop @server/dev-serv))
