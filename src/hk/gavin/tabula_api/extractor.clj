(ns hk.gavin.tabula-api.extractor
  "Core mechanism of table extraction from PDF file.

  It is the bridge between Clojure hash-map extraction options and the
  tabula-java CLI module."
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as string])
  (:import (org.apache.commons.cli DefaultParser)
           (technology.tabula CommandLineApp)))

(def option-types
  "Mapping of option name to its option type.
  Option types include sting-arg, multi-string-arg and boolean-flag."
  {:area :multi-string-arg :columns :string-arg :format :string-arg
   :guess :boolean-flag :lattice :boolean-flag :pages :string-arg
   :password :string-arg :stream :boolean-flag})

(defn option-truthy?
  "Returns true if v is truthy for a value, false otherwise.
  nil, \"false\" and \"no\" is considered falsy."
  [v]
  (match v
    (:or nil "false" "no") false
    :else                  true))

(defn string-arg-option->string-list
  "Convert a string-arg option into a list of CLI argument string."
  [[k v]]
  (if (some? v) [(str "--" (name k)) v] []))

(defmulti option->string-list
  "Convert an element of the option map into a list of CLI argument string.

  Ineffective and unsupported options were mapped to empty list."
  (fn [[k _]] (get-in option-types k :unsupported)))

(defmethod option->string-list :string-arg [m]
  (string-arg-option->string-list m))

(defmethod option->string-list :multi-string-arg [[k v]]
  (if (vector? v)
    (mapcat #(string-arg-option->string-list [k %1]) v)
    (string-arg-option->string-list [k v])))

(defmethod option->string-list :boolean-flag [[k v]]
  (if (option-truthy? v) [(str "--" (name k))] []))

(defmethod option->string-list :unsupported [_]
  [])

(defn option-map->string-list
  "Convert a full option map into a list of CLI argument string."
  [option-map]
  (mapcat option->string-list option-map))

(defn option-map->command-line
  "Convert a full option map into a Apache commons CommanLine object.
  The arguments will be parsed against the options definition from tabula-java."
  [option-map]
  (let [parser (DefaultParser.)
        build-options (CommandLineApp/buildOptions)
        args (-> option-map option-map->string-list into-array)]
    (.parse parser build-options args)))

(defn extract-tables
  "Extract the tables from pdf-file against the options specified in option-map.
  The results will be written into out-file."
  [option-map pdf-file out-file]
  (let [cmd-line (option-map->command-line option-map)
        cli-app (CommandLineApp. System/out cmd-line)]
    (.extractFileInto cli-app pdf-file out-file)))
