(ns hk.gavin.tabula-api.extractor
  "Core mechanism of table extraction from PDF file.

  It is the bridge between Clojure hash-map extraction options and the
  tabula-java CLI module."
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as string])
  (:import (org.apache.commons.cli DefaultParser)
           (org.apache.tika Tika)
           (technology.tabula CommandLineApp)))

(def option-types
  "Mapping of option name to its option type.
  Option types include sting-arg, multi-string-arg and boolean-flag."
  {:area :multi-string-arg :columns :string-arg :format :string-arg
   :guess :boolean-flag :lattice :boolean-flag :pages :string-arg
   :password :string-arg :stream :boolean-flag})

(def boolean-flag-values
  "Mapping of boolean flag string value to boolean type.
  true, \"true\" and \"True\" is considered truthy.
  false, \"false\" and \"False\" is considered falsy."
  {true true "true" true "True" true
   false false "false" false "False" false})

(defn string-arg-option->string-vector
  "Convert a string-arg option into a vector of CLI argument string."
  [[k v]]
  (if (string? v) [(str "--" (name k)) v] []))

(defmulti option->string-vector
  "Convert an element of the option map into a vector of CLI argument string.

  Ineffective and unsupported options were mapped to empty vector."
  (fn [[k _]] (get option-types k :unsupported)))

(defmethod option->string-vector :string-arg [m]
  (string-arg-option->string-vector m))

(defmethod option->string-vector :multi-string-arg [[k v]]
  (if (vector? v)
    (mapcat #(string-arg-option->string-vector [k %1]) v)
    (string-arg-option->string-vector [k v])))

(defmethod option->string-vector :boolean-flag [[k v]]
  (let [value (get boolean-flag-values v)]
    (if (true? value) [(str "--" (name k))] [])))

(defmethod option->string-vector :unsupported [_]
  [])

(defn option-map->string-vector
  "Convert a full option map into a vector of CLI argument string."
  [option-map]
  (mapcat option->string-vector option-map))

(defn option-map->command-line
  "Convert a full option map into a Apache commons CommanLine object.
  The arguments will be parsed against the options definition from tabula-java."
  [option-map]
  (let [parser (DefaultParser.)
        build-options (CommandLineApp/buildOptions)
        args (-> option-map option-map->string-vector into-array)]
    (.parse parser build-options args)))

(def tika
  "A lazily created Apache Tika instance."
  (delay (Tika.)))

(defn validate-pdf-file
  "Validate if pdf-file exists and is really a PDF file."
  [pdf-file]
  (or (instance? java.io.File pdf-file)
      (throw (IllegalArgumentException. "file is missing.")))
  (or (= (.detect @tika pdf-file) "application/pdf")
      (throw (IllegalArgumentException. "file is not a valid PDF file.")))
  true)

(defn extract-tables
  "Extract the tables from pdf-file against the options specified in option-map.
  The results will be written into out-file."
  [option-map pdf-file out-file]
  (let [cmd-line (option-map->command-line option-map)
        cli-app (CommandLineApp. System/out cmd-line)]
    (.extractFileInto cli-app pdf-file out-file)))
