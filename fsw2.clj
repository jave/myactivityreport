#!/bin/bash
#_(
   #_You can put other options here
   OPTSxxx='
   -J-Xms256m -J-Xmx256m -J-client
   '
   . settings.sh
   echo exec clojure $OPTS "$0" "$@"
   exec clojure $OPTS "$0" "$@"
   )

(ns fsw2
  (:require [clojure.string :as str]
            [shared]
            [java-time :as t]
            [clojure.java.io :as io])
  ;;(:use clojure.java.io)
  )

(println "fileing... ")
;; working on my plans for instance, looks like:
;; 2022-06-13T00:51:18Z | /home/joakim/roles/Plans/TransHumanism.org | MODIFY



(defn date-to-key [date]
  "convert date to a truncated key, which can be used in a hashmap"
  (t/format "yyyyMMdd" (t/truncate-to
                        (t/zoned-date-time
                         date
                         (t/zone-id "Europe/Stockholm")) :days)))


(defn parseline [inline]
  (try 
    (let [line (str/split inline #" \| " )
          time (try (t/zoned-date-time (first line) )
                    (catch Exception e))
          file (try   (second line)
                      (catch Exception e))
          event (try (nth line 2)
                     (catch Exception e))
          after (t/after? time (t/minus (t/zoned-date-time) (t/days shared/num-days)))
          ]
      (if after
        (list (date-to-key time) file event)
        nil))
    
    (catch Exception e)
    ) ;; i would prefer a pipeline pattern, also somehow filter away useless lines before parsing, in the filter
  )

(defn validline [event]
  (if (or (nil? event)(nil? (first event))) nil event))

(defn validfile [event]
  (if (or (nil? event) (not (.exists (io/file (second event))))  ) nil event))
  


(defn ignore-event? [event]
  ;; this should be loaded from edn file
  (if (or (nil? event)(re-find #"/\.|/tmp/|navidrome.db|/home/joakim/go" (second event) )) nil event)
  )

(defn syncbind-workaround [event]
  (if (nil? event) nil 
      (list  (first event)                          
             (str/replace (second event) "/syncbind/" "/roles/" ) ;; workaround for a syncthing bindmount i have
             (nth event 2) ))
  )

(defn truncate-event-filenames [event]
  (if (nil? event) nil (list  (first event)
                              ;;(re-find #"^.*/Plans|^.*/art|^.*" (second %))
                              ;; these are "roots" and should be in a edn file
                              ;; truncate filenames a bit, for birds eye view, if no trunc pattern is found return original
                              (str/replace (first (re-find #"^/home/joakim/roles(/[^/]*)|^/flib(/[^/]*)|^/home/joakim(/[^/]*)|^.*"  (second event))) #"^/home/joakim" "~")
                              (nth event 2) ))
  )


(defn line-to-event [line]
   (->> (parseline line)
        (validline)
        (validfile)
        (ignore-event?)
        (syncbind-workaround)
        (truncate-event-filenames)
        ))

(defn update-events-compact [events-compact line]
  ;; here i want to test each row, if its to be discarded do it immediately and go on to the next test
  ;; finally add it to events-compact in its filtered form, if it survived the culling
  (let [processed-event  (line-to-event line)
        ]
    
    (if (not (nil? processed-event))
      (-> (update-in events-compact
                     [(second processed-event ) ;; the filename
                      (first processed-event) ;; the truncated date
                      ]
                     #(if (nil? %) 1 (inc %)))
          (update-in 
                     [(second processed-event ) ;; the filename
                      "total"
                      ]
                     #(if (nil? %) 1 (inc %)))
          )
      events-compact
      )))

(def fsw-events
  (-> (reduce update-events-compact {}  (take-last 10000000  (line-seq (io/reader "filesystemwatch.log"))))
      (shared/add-tags-from-file , "fsw-tags.edn" ))
  )


(shared/write-reports "filewatch-report" fsw-events)
