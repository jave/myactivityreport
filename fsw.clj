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

(ns fsw)
(require '[java-time :as t])
(require '[shared])

(println "fileing... ")

(def num-days 30) ;; 30 days back


;; working on my plans for instance, looks like:
;; 2022-06-13T00:51:18Z | /home/joakim/roles/Plans/TransHumanism.org | MODIFY


(use 'clojure.java.io)
(require '[clojure.string :as str])

(defn date-to-key [date]
  "convert date to a truncated key, which can be used in a hashmap"
  (t/format "yyyyMMdd" (t/truncate-to
                        (t/zoned-date-time
                         date
                         (t/zone-id "CET")) :days)))


(defn parseline [inline]
  (try 
    (let [line (str/split inline #" \| " )
          time (try (t/zoned-date-time (first line))
                    (catch Exception e))
          file (try   (second line)
                      (catch Exception e))
          event (try (nth line 2)
                      (catch Exception e))
          ]
      (list (date-to-key time) file event))
    (catch Exception e)
    ) ;; i would prefer a pipeline pattern, also somehow filter away useless lines before parsing, in the filter
  )

(defn validline [parsedline]
  (not (nil? (first parsedline))))

(defn update-events-compact [events-compact event]
  (update-in events-compact
          [(second event ) ;; the filename
           (first event) ;; the truncated date
           ]
          #(if (nil? %) 1 (inc %))))

;;(def events-compact (reduce update-events-compact {} (filteredlog)) )

(defn filteredlog [filename eventcount]
  (->> (map parseline (take-last eventcount (line-seq (reader filename)))) ;; the last 100 parsed lines from the log
       (filter validline ) ;; keep only valid lines, that match a regexp
       (filter #(nil? (re-find #"/\.|/tmp" (second %) )) ) ;; dont care about dotfiles for now, tmpfiles, throw them away

       (map #(list  (first %)
                    (str/replace (second %) "/syncbind/" "/roles/" ) ;; workaround for a syncthing bindmount i have
                    (nth % 2) ) )
       
       
       (map #(list  (first %)
                    ;;(re-find #"^.*/Plans|^.*/art|^.*" (second %))
                    (first (re-find #"^/home/joakim/roles(/[^/]*)|^/flib(/[^/]*)|^/home/joakim(/[^/]*)|^.*"  (second %)))        ;; truncate filenames a bit, for birds eye view, if no trunc pattern is found return original
                    (nth % 2) ) )
       
       )
  )

(def fsw-events
   (reduce update-events-compact {}  (filteredlog "filesystemwatch.log" 1000000) ))

;;(println fsw-events)

;;(println (str/join (map #(str (render (first %) (second %)) "\n")   fsw-events)))

(spit "out/filewatch-report.txt" (str/join (map #(str (shared/render (first %) (second %)) "\n")   fsw-events)))
(spit "out/filewatch-report.html" (str/join (map #(str (shared/render-tr (first %) (second %)) "\n")   fsw-events)))
(println "wrote filewatch-report.txt")
