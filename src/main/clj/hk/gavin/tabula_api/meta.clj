(ns hk.gavin.tabula-api.meta
  "Metadata for the tabula-api project itself."
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.util Properties)))

(def version-string
  "Version string extracted lazily from POM properties."
  (delay
   (let [pom (-> "META-INF/maven/hk.gavin/tabula-api/pom.properties"
                 io/resource io/reader)
         props (doto (Properties.) (.load pom))]
     (.get props "version"))))

; string->env-key, java-env-map->env-map and conf are derived from environ
; by James Reeves. Which is distributed under Eclipse Public License.
; https://github.com/weavejester/environ

(defn- string->env-key
  "Convert environment and property key to lower case hyphenated keyword."
  [k]
  (-> k str/lower-case (str/replace "_" "-") (str/replace "." "-") keyword))

(defn- java-env-map->env-map
  "Convert Java envorinment and property map to Clojure map."
  [m]
  (->> m (map (fn [[k v]] [(string->env-key k) v])) (into {})))

(def ^:private default-conf
  "Default configuration map."
  {:host "localhost"
   :port "8080"})

(def conf
  "Configuration map.

  Precedence: Defaults < Environment variables < Java system properties."
  (delay
   (merge default-conf
          (java-env-map->env-map (System/getenv))
          (java-env-map->env-map (System/getProperties)))))
