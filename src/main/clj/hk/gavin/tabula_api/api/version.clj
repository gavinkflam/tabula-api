(ns hk.gavin.tabula-api.api.version
  "Show version information."
  (:require [hk.gavin.tabula-api.meta :as meta]))

(defn version
  "Devire response map to respond with version string."
  [_]
  {:status 200 :body @meta/version-string})

(def routes
  "Routes for the version endpoint"
  #{["/api/version" :get [`version]]})
