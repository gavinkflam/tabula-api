(ns hk.gavin.tabula-api.api.extract
  (:require [clojure.walk :as walk]
            [io.pedestal.http.content-negotiation :as neg]
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

(def supported-mime-types
  ["text/csv" "application/json" "text/tab-separated-values"])

(def apply-content-type
  {:name ::apply-content-type
   :leave (fn [context]
            (cond-> context
              (= 200 (get-in context [:response :status]))
              (update-in
               [:response :headers "Content-Type"]
               (-> context (get-in [:request :accept :field]) constantly))))})

(defn throw-illegal-mime-type
  [mime-type]
  (throw (IllegalArgumentException. (str mime-type " is not supported."))))

(defn no-mime-types-match
  [context]
  (-> context (get-in [:request :headers "accept"]) throw-illegal-mime-type))

(def content-negotiator
  (neg/negotiate-content supported-mime-types
                         {:no-match-fn no-mime-types-match}))

(defn mime-type->format
  [mime-type]
  (case mime-type
    "text/csv"                  "CSV"
    "application/json"          "JSON"
    "text/tab-separated-values" "TSV"
    (throw-illegal-mime-type)))

(defn request->option-map
  [request]
  (let [out-format (-> request (get-in [:accept :field]) mime-type->format)]
    (-> request (get :params) (dissoc "file") (walk/keywordize-keys)
        (merge {:format out-format}))))

(defn extract
  [request]
  (let [params (request->option-map request)
        pdf-file (get-in request [:params "file" :tempfile])
        out-file (java.io.File/createTempFile "out-file" ".tmp")]
    (extractor/validate-pdf-file pdf-file)
    (extractor/extract-tables params pdf-file out-file)
    {:status 200 :body out-file}))

(def routes
  #{["/api/extract" :post [apply-content-type
                           extract-error-handler
                           content-negotiator
                           (middlewares/multipart-params)
                           `extract]]})
