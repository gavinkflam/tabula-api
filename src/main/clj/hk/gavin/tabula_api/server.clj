(ns hk.gavin.tabula-api.server
  "Pedestal server and the entry-point."
  (:gen-class) ; for -main method in uberjar
  (:require [io.pedestal.http :as http]
            [hk.gavin.tabula-api.service :as service]))

(defonce prod-serv
  (delay (http/create-server service/service)))

(defonce dev-serv
  (delay
   (-> service/service ;; start with production configuration
       (merge {:env :dev
                ;; do not block thread that starts web server
               ::http/join? false
                ;; Routes can be a function that resolve routes,
                ;;  we can use this to set the routes to be reloadable
               ::http/routes #(deref #'service/routes)
                ;; all origins are allowed in dev mode
               ::http/allowed-origins {:creds true
                                       :allowed-origins (constantly true)}})
        ;; Wire up interceptor chains
       http/default-interceptors
       http/dev-interceptors
       http/create-server)))

(defn run-dev
  "Create the development server if not yet created, and start the server."
  []
  (http/start @dev-serv))

(defn -main
  "The entry-point for 'lein run' and uberjar."
  [& args]
  (http/start @prod-serv))
