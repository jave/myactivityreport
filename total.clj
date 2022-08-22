#!/bin/bash
#_(
   #_You can put other options here
   OPTS='-J-Xms256m -J-Xmx256m -J-client'
   . settings.mk   
   exec clojure $OPTS "$0" "$@"
   )
(ns total
  (:require [java-time :as t]
            [clojure.string :as str]
            [shared]
            [clojure.java.io :as io] )
  (:use hiccup.core)
  )


;;read out/*-report.edn
;;(filter #(.endsWith (.getName %) "-report.edn") (file-seq (io/file ".")))
;; into a common struct
;; dump to file, same way i do the other reports


;;(merge (clojure.edn/read-string (slurp "out/gitlab-report.edn")) (clojure.edn/read-string  (slurp "out/nudges-report.edn")))


(let [files (filter #(.endsWith (.getName %) "-report.edn") (file-seq (io/file ".")))
      events (reduce #(merge %1 (clojure.edn/read-string (slurp %2))) {} files)]
  (shared/write-reports "total-report" events)
)
