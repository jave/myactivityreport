#!/bin/bash
#_(
   #_You can put other options here
   OPTS='-J-Xms256m -J-Xmx256m -J-client'
   . settings.mk   
   exec clojure $OPTS "$0" "$@"
   )


;; parse 
;; create datelist
(ns myhabit
  (:require [clojure.string :as str]
            [shared]
            [java-time :as t]
            [clojure.java.io :as io])
  )

(def     events-compact {})

(defn update-events-compact [events-compact event]
  ;; event is list of label, date (and tags maybe)
  (update-in events-compact [(first event) (second event)])
)

(defn parse-habit [event]

  (let [datelist (map shared/date-to-key (shared/datelist))
        symlist (str/split (subs event 81 112) #"")
        label0 (str/split (str/trim (subs event 0 78)) #":")
        tag0 (first label0)
        label (str/trim (str/replace (second label0) #"TODO" ""))
        tags (remove #(= "" %) (conj (str/split (str/trim (subs event 112 )) #":") tag0))
        hashmapwithnils (into {} (map #(hash-map %1 (if (= %2 "*") 1) ) datelist symlist))
        
        date-events (reduce-kv (fn [new-map k v] (if (not (nil? v)) (assoc new-map k v) new-map)) {} hashmapwithnils)
        total (count date-events)
        ]
    (list label (assoc date-events "tags" tags "total" total) )))


;;(parse-habit   "TransHumanism:      TODO nudge                                                ******************************!                                                                            :nudge::0day:")

(def events-compact (reduce  (fn [a b] (assoc a (first b) (second b)))
                             {} 
                             (map parse-habit (line-seq (io/reader "out/agenda.txt")))))


(shared/write-reports "habit-report" events-compact)
