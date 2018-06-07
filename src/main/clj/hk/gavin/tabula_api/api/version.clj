(ns hk.gavin.tabula-api.api.version
  (:require [hk.gavin.tabula-api.meta :as meta]))

(defn version
  [_]
  {:status 200 :body @meta/version-string})

(def routes
  #{["/api/version" :get [`version]]})
