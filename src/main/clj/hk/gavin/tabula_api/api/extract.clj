(ns hk.gavin.tabula-api.api.extract
  (:require [clojure.walk :as walk]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.interceptor.error :as error-int]
            [io.pedestal.log :as log]
            [hk.gavin.tabula-api.extractor :as extractor]))

(defn log-pedestal-exception
  [ctx ex code]
  (let [cause (-> ex ex-data :exception)
        error-name (-> cause class .getSimpleName)
        log-message (str error-name ": " (.getMessage cause))
        path (get-in ctx [:request :path-info])]
    (if (<= 400 code 499)
      (log/info :msg log-message :path path)
      (log/error :msg log-message :path path :ctx ctx :exception cause))))

(defn pedestal-exception->response
  [ctx ex code]
  (let [message (-> ex ex-data :exception .getMessage)]
    (log-pedestal-exception ctx ex code)
    (assoc ctx :response {:status code :body message})))

(def extract-error-handler
  (error-int/error-dispatch
   [ctx ex]
   [{:exception-type (:or :org.apache.commons.cli.ParseException
                          :java.lang.IllegalArgumentException)}]
   (pedestal-exception->response ctx ex 400)
   :else
   (pedestal-exception->response ctx ex 500)))

(defn params->option-map
  [params]
  (-> params (dissoc "file") (walk/keywordize-keys)))

(defn extract
  [request]
  (let [params (-> request (get :params) params->option-map)
        pdf-file (get-in request [:params "file" :tempfile])
        out-file (java.io.File/createTempFile "out-file" ".tmp")]
    (extractor/validate-pdf-file pdf-file)
    (extractor/extract-tables params pdf-file out-file)
    {:status 200 :body out-file}))

(def routes
  #{["/api/extract" :post [extract-error-handler
                           (middlewares/multipart-params)
                           `extract]]})
