(ns hk.gavin.tabula-api.api.version)

(def version-string
  (delay (System/getProperty "hk.gavin.tabula-api.version")))

(defn version
  [_]
  {:status 200 :body @version-string})

(def routes
  #{["/api/version" :get [`version]]})
