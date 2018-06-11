(ns hk.gavin.tabula-api.test-util
  (:require [clojure.java.io :as io]
            [clj-http.client :as client]
            [io.pedestal.http :as http]
            [hk.gavin.tabula-api.server :as server]))

; IO utilities
(defn resource-file
  [file-name]
  (-> file-name io/resource io/file))

; Request utilities
(def base-req
  {:method :get
   :scheme :http
   :server-name "localhost"
   :server-port 8080
   :throw-exceptions false})

(defn request
  [req]
  (client/request (merge base-req req)))

; Form utilities
(defn update-form
  [form name f-target f-others]
  (->> form
       (map #(if (= name (get % :name)) (f-target %) (f-others %)))
       (remove empty?)))

(defn update-field
  [form name content]
  (update-form form name #(assoc % :content content) identity))

(defn except-field
  [form name]
  (update-form form name (constantly {}) identity))

(defn only-field
  [form name]
  (update-form form name identity (constantly {})))

; Server lifecycle utilities
(defn stop-dev-serv
  []
  (http/stop @server/dev-serv))

(defn stop-prod-serv
  []
  (http/stop @server/prod-serv))

; Fixtures
(defn dev-server-running-fixture
  [f]
  (server/run-dev)
  (f)
  (stop-dev-serv))

(defn no-servers-running-fixture
  [f]
  (stop-dev-serv)
  (stop-prod-serv)
  (f))
