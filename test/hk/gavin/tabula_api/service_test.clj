(ns hk.gavin.tabula-api.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.test :refer :all]
            [io.pedestal.http :as http]
            [hk.gavin.tabula-api.service :as service]))

(def service
  (::http/service-fn (http/create-servlet service/service)))

(deftest hello-world-test
  (is (=
       (:body (response-for service :get "/hello"))
       "hello world.\n")))

(deftest hello-world-with-name-test
  (is (=
       (:body (response-for service :get "/hello?name=gavin"))
       "hello gavin.\n")))
