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
(ns dates)
(require '[java-time :as t])
(require '[shared])
(println "dates")

(spit "out/dates.html"(shared/render-tr-dates))
