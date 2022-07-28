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
(ns ff
  (:require [next.jdbc :as jdbc]
            [lambdaisland.uri :refer [uri join]]
            [java-time :as t]
            [shared]
            [clojure.string :as str])  )


(def db {:dbtype "sqlite" :dbname "places.sqlite"})
(def ds (jdbc/get-datasource db))
;; (clojure.pprint/pprint (jdbc/execute! ds ["select *, datetime(visit_date/1000000,'unixepoch') AS visit_date_readable  from moz_historyvisits as v, moz_places as p where v.place_id=p.id and p.url = ? order by v.visit_date desc limit 100; "
;;                                           "https://gitlab.com/arbetsformedlingen/devops/cert-monitoring/-/issues/2"

;; (def events-raw (jdbc/execute! ds ["select *, datetime(visit_date/1000000,'unixepoch') AS visit_date_readable  from moz_historyvisits as v, moz_places as p where v.place_id=p.id and p.url like ? order by v.visit_date desc limit 100; "
;;                                           "https://gitlab.com%"
;;                                ]))

(def events-raw (jdbc/execute! ds ["select *, datetime(visit_date/1000000,'unixepoch') AS visit_date_readable  from moz_historyvisits as v, moz_places as p where v.visit_date > ? and v.place_id=p.id order by v.visit_date desc limit 100000; "
                                   (* 1000000 (.getEpochSecond (t/minus (t/instant) (t/days shared/num-days) ))) ;; today minus 30 days, in the timestamp format firefox employs
                                   ]))

(defn update-events-compact [events-compact event]
  (->
   ;; this updates the event ticker for the current events date
   (update-in events-compact [(:host (uri (:moz_places/url event)))
                              (shared/date-to-key (t/instant (/ (:moz_historyvisits/visit_date event) 1000)))
                              ]
              #(if (nil? %) 1 (inc %)))
   ;; this updates the "total" ticker for the current event
   (update-in  [(:host (uri (:moz_places/url event)))
                "total"
                              ]
              #(if (nil? %) 1 (inc %)))
   )
             )
  

(def events-compact
  (sort-by #(get  (second %) "total") > 
           (reduce update-events-compact {} events-raw)))



(shared/write-reports "ff-report" events-compact)
