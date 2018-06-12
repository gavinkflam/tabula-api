(ns hk.gavin.tabula-api.api.extract
  "API endpoint for extracting tables from PDF file."
  (:require [clojure.walk :as walk]
            [io.pedestal.http.content-negotiation :as neg]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.interceptor.error :as error-int]
            [io.pedestal.log :as log]
            [hk.gavin.tabula-api.extractor :as extractor])
  (:import (java.io File)))

; Content type header handling
(def ^:private content-type-header-interceptor
  "Interceptor to set Content-Type header based on Accept header."
  {:name ::apply-content-type
   :leave (fn [context]
            (cond-> context
              (= 200 (get-in context [:response :status]))
              (assoc-in
               [:response :headers "Content-Type"]
               (get-in context [:request :accept :field]))))})

; Error handling
(defn- log-pedestal-exception
  "Log the error message and request path.

  Log the context map and cause as well for unknown errors."
  [ctx ex code]
  (let [cause (-> ex ex-data :exception)
        error-name (-> cause class .getSimpleName)
        log-message (str error-name ": " (.getMessage cause))
        path (get-in ctx [:request :path-info])]
    (if (<= 400 code 499)
      (log/info :msg log-message :path path)
      (log/error :msg log-message :path path :ctx ctx :exception cause))))

(defn- handle-error
  "Log the error and add error response to the context map."
  [ctx ex code]
  (let [message (-> ex ex-data :exception .getMessage)]
    (log-pedestal-exception ctx ex code)
    (assoc ctx :response {:status code :body message})))

(def ^:private error-interceptor
  "Interceptor to handle error based on error types."
  (error-int/error-dispatch
   [ctx ex]
   [{:exception-type (:or :org.apache.commons.cli.ParseException
                          :java.lang.IllegalArgumentException)}]
   (handle-error ctx ex 400)
   :else
   (handle-error ctx ex 500)))

; Content type netogiation
(def mime-types
  "Supported MIME types to file extension map."
  {"text/csv" "CSV"
   "application/json" "JSON"
   "text/tab-separated-values" "TSV"})

(defn- no-mime-types-match
  "Throw error when no mime type matched."
  [context]
  (let [mime-type (get-in context [:request :headers "accept"])]
    (throw (IllegalArgumentException. (str mime-type " is not supported.")))))

(def ^:private content-type-negotiation-interceptor
  "Interceptor for content type negotiation."
  (neg/negotiate-content (keys mime-types)
                         {:no-match-fn no-mime-types-match}))

; Real extraction handling
(defn- request->option-map
  "Derive the extraction option map from request map."
  [request]
  (let [f (as-> request x (get-in x [:accept :field]) (get mime-types x))]
    (-> request (get :params) (dissoc "file") (walk/keywordize-keys)
        (merge {:format f}))))

(defn- extract
  "Extract tables and derive response map. Throw if any errors."
  [request]
  (let [params (request->option-map request)
        pdf-file (get-in request [:params "file" :tempfile])
        out-file (File/createTempFile "out-file" ".tmp")]
    (extractor/validate-pdf-file pdf-file)
    (extractor/extract-into params pdf-file out-file)
    {:status 200 :body out-file}))

(def routes
  "Routes for the extract endpoint."
  #{["/api/extract" :post [content-type-header-interceptor
                           error-interceptor
                           content-type-negotiation-interceptor
                           (middlewares/multipart-params)
                           `extract]]})
