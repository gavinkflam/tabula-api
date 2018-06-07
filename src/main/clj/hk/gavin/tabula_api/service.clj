(ns hk.gavin.tabula-api.service
  (:require [clojure.set :as set]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [hk.gavin.tabula-api.api.extract :as api-extract]))

(def routes
  (route/expand-routes (set/union api-extract/routes)))

(def service {:env                 :prod
              ::http/routes        routes
              ::http/resource-path "/public"
              ::http/type          :jetty
              ::http/port          8080})
