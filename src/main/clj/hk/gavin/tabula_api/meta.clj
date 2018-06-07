(ns hk.gavin.tabula-api.meta
  (:require [clojure.java.io :as io])
  (:import (java.util Properties)))

(def version-string
  (delay
   (let [pom (-> "META-INF/maven/hk.gavin/tabula-api/pom.properties"
                 io/resource io/reader)
         props (doto (Properties.) (.load pom))]
     (.get props "version"))))
