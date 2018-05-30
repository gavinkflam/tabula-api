(ns hk.gavin.tabula-api.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn hello-world
  [request]
  (let [name (get-in request [:params :name] "world")]
    {:status 200 :body (str "hello " name ".\n")}))

(def routes
  (route/expand-routes
   #{["/hello" :get hello-world :route-name :hello-world]}))

(def service {:env                 :prod
              ::http/routes        routes
              ::http/resource-path "/public"
              ::http/type          :jetty
              ::http/port          8080})
