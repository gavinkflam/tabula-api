(ns hk.gavin.tabula-api.meta
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.util Properties)))

(def version-string
  (delay
   (let [pom (-> "META-INF/maven/hk.gavin/tabula-api/pom.properties"
                 io/resource io/reader)
         props (doto (Properties.) (.load pom))]
     (.get props "version"))))

; keywordize, java-map->map and env are derived from environ by James Reeves.
; Distributed under Eclipse Public License.
; https://github.com/weavejester/environ

(defn- keywordize [s]
  (-> s str/lower-case (str/replace "_" "-") (str/replace "." "-") keyword))

(defn- java-map->map
  [m]
  (->> m (map (fn [[k v]] [(keywordize k) v])) (into {})))

(def default-env
  {:host "localhost"
   :port "8080"})

(defonce env
  (merge default-env
         (java-map->map (System/getenv))
         (java-map->map (System/getProperties))))
