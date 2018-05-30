(ns hk.gavin.tabula-api.service
  (:require [clojure.walk :as walk]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [hk.gavin.tabula-api.extractor :as extractor]))

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
   [[["/extract_tables" ^:interceptors [(middlewares/multipart-params)]
      {:post `extract-tables}]]]))

(def service {:env                 :prod
              ::http/routes        routes
              ::http/resource-path "/public"
              ::http/type          :jetty
              ::http/port          8080})
