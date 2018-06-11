(ns hk.gavin.tabula-api.test-util
  (:require [clojure.java.io :as io]
            [clj-http.client :as client]
            [io.pedestal.http :as http]
            [hk.gavin.tabula-api.server :as server]))

(defn resource-file
  [file-name]
  (-> file-name io/resource io/file))

(def base-req
  {:method :get
   :scheme :http
   :server-name "localhost"
   :server-port 8080
   :throw-exceptions false})

(defn request
  [req]
  (client/request (merge base-req req)))

(defn update-field
  [form name content]
  (map #(if (= (get % :name) name) (assoc % :content content) %) form))

(defn dev-server-running-fixture
  [f]
  (server/run-dev)
  (f)
  (http/stop @server/dev-serv))

(defn stop-dev-serv
  []
  (http/stop @server/dev-serv))

(defn stop-prod-serv
  []
  (http/stop @server/prod-serv))

(defn no-servers-running-fixture
  [f]
  (stop-dev-serv)
  (stop-prod-serv)
  (f))
