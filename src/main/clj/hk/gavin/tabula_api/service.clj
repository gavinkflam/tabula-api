(ns hk.gavin.tabula-api.service
  (:require [clojure.walk :as walk]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.interceptor.error :as error-int]
            [hk.gavin.tabula-api.extractor :as extractor]))

(def service-error-handler
  (error-int/error-dispatch
   [ctx ex]
   [{:exception-type :org.apache.commons.cli.ParseException}]
   (assoc ctx :response {:status 400
                         :body (-> ex ex-data :exception .getMessage)})))

(defn params->option-map
  [params]
  (-> params (dissoc "file") (walk/keywordize-keys)))

(defn extract-tables
  [request]
  (let [params (-> request (get :params) params->option-map)
        pdf-file (get-in request [:params "file" :tempfile])
        out-file (java.io.File/createTempFile "out-file" ".tmp")]
    (extractor/extract-tables params pdf-file out-file)
    {:status 200 :body out-file}))

(def routes
  (route/expand-routes
   #{["/extract_tables" :post [service-error-handler
                               (middlewares/multipart-params)
                               `extract-tables]]}))

(def service {:env                 :prod
              ::http/routes        routes
              ::http/resource-path "/public"
              ::http/type          :jetty
              ::http/port          8080})
