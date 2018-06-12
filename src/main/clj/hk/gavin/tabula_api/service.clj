(ns hk.gavin.tabula-api.service
  (:require [clojure.set :as set]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [hk.gavin.tabula-api.meta :refer [conf]]
            [hk.gavin.tabula-api.api.extract :as api-extract]
            [hk.gavin.tabula-api.api.version :as api-version]))

(def routes
  (route/expand-routes (set/union api-extract/routes
                                  api-version/routes)))

(def service {:env                 :prod
              ::http/routes        routes
              ::http/resource-path "/public"
              ::http/type          :immutant
              ::http/host          (@conf :host)
              ::http/port          (@conf :port)})
