(ns hk.gavin.tabula-api.test-util
  "Utilities for tests."
  (:require [clojure.java.io :as io]
            [clj-http.client :as client]
            [io.pedestal.http :as http]
            [hk.gavin.tabula-api.server :as server]))

; IO utilities
(defn resource-file
  "Open the resource file by name."
  [file-name]
  (-> file-name io/resource io/file))

; Request utilities
(def ^:private base-request-map
  "Base request map for endpoints."
  {:method :get
   :scheme :http
   :server-name "localhost"
   :server-port 8080
   :throw-exceptions false})

(defn request
  "Make a request with the extra request map merged with the base request map."
  [request-map]
  (client/request (merge base-request-map request-map)))

; Form utilities
(defn update-form
  "Update fields with the given transform functions. Empty fields are removed."
  [form name f-target f-others]
  (->> form
       (map #(if (= name (get % :name)) (f-target %) (f-others %)))
       (remove empty?)))

(defn update-field
  "Update content of the fields with the given name."
  [form name content]
  (update-form form name #(assoc % :content content) identity))

(defn except-field
  "Remove the fields with the given name."
  [form name]
  (update-form form name (constantly {}) identity))

(defn only-field
  "Remove all fields from the form except the fields with the given name."
  [form name]
  (update-form form name identity (constantly {})))

; Server lifecycle utilities
(defn stop-dev-serv
  "Stop the development server."
  []
  (http/stop @server/dev-serv))

(defn stop-prod-serv
  "Stop the production server."
  []
  (http/stop @server/prod-serv))

; Fixtures
(defn dev-server-running-fixture
  "Ensure the development server is running before test. Stop it afterwards."
  [f]
  (server/run-dev)
  (f)
  (stop-dev-serv))

(defn no-servers-running-fixture
  "Ensure no servers are running before test."
  [f]
  (stop-dev-serv)
  (stop-prod-serv)
  (f))
