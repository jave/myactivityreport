#!/bin/bash
#_(
   #_You can put other options here
   OPTS='
   -J-Xms256m -J-Xmx256m -J-client
   '
   . settings.sh
   echo exec clojure $OPTS "$0" "$@"
   exec clojure $OPTS "$0" "$@"
   )

(ns nudges)
(require '[java-time :as t])
(require '[shared])
(println "dates")

;;(spit "out/dates.html" (shared/render-tr-dates))

(def events-compact 
  (-> (clojure.edn/read-string(slurp "out/nudges.edn"))
      (shared/add-class , "nudge")
      (shared/add-prio ,)
      (shared/add-days , )))

(shared/write-reports "nudges-report" events-compact)
