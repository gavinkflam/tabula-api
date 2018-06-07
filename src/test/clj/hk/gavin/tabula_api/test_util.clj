(ns hk.gavin.tabula-api.test-util
  (:require [clojure.java.io :as io]
            [clj-http.client :as client]))

(defn resource-file
  [file-name]
  (-> file-name io/resource io/file))

(def base-req
  {:scheme :http
   :server-name "localhost"
   :server-port 8080
   :throw-exceptions false})

(defn request
  [req]
  (client/request (merge base-req req)))

(defn update-field
  [form name content]
  (map #(if (= (get % :name) name) (assoc % :content content) %) form))
