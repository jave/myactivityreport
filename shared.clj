;;(use 'hiccup.core)
;;the gitlab api returns utc timestamps
;; truncate them to yyyyMMdd, discarding the time part
;; convert to string so it can be used as a key in a hashmap
;; zoned-date-time didnt work as keys, because they are "value based"
(ns shared
  (:require [java-time :as t]
            [clojure.string :as str])
  (:use hiccup.core)
  )



(defn date-to-key [date]
  "convert date to a truncated key, which can be used in a hashmap"
  (t/format "yyyyMMdd" (t/truncate-to
                        (t/zoned-date-time
                         date
                         (t/zone-id "CET")) :days))
  )
(def num-days 30) ;; 30 days back
(defn daterender [date label-events]
  (format "%s"
          ;; render done/fail, similar to org-habits
          (if (nil? (get label-events (date-to-key date))) " " "*"  )
          )
  
  )

(defn daterender-debug [date label-events]
  (format "[%s %s %d]"
          ;; render done/fail, similar to org-habits
          (if (nil? (get label-events (date-to-key date))) "_" "*"  )

          ;; render date, if wanted
           (t/format "yyyyMMdd"
                     (t/zoned-date-time date (t/zone-id)))
          ;; render number of events, if wanted
           (get label-events (date-to-key date))
           
          
          )
  
  )
        ;; render done/fail, unicode
  
;;                    (if (nil? (get events-compact (date-to-key date))) "❌" "✅"  )


(defn daterender-td [date events-compact]
  (let [levelraw (get events-compact (date-to-key date))
        levelnum (if (nil? levelraw) 0 levelraw  )
        levelsym (if (nil? levelraw) " " "*"  )
        level (condp >= levelnum
                0 0
                2 1
                8 2
                10 3
                4
                )]
    (html [:td {:level level} 
           
           (format "%s"
                   levelsym
                   )]))
  
  )

(defn datelist[]
  "30 days backwards"
  (reverse (take (- num-days 0) ;; it used to be num-days -1 dunno why atm
                 (t/iterate t/minus (t/truncate-to (t/instant) :days) (t/days 1))))
  )

(defn render-tr-dates []
  (html [:thead [:tr  [:th (t/format "yyyy-MM-dd HH:mm" (t/local-date-time))]  
                  
                  (clojure.string/join  (map
                                         #(html [:td {:class "vdate"} (t/format "yyyy-MM-dd" (t/zoned-date-time % (t/zone-id))) ])
                                         (datelist) )
                                        )
                  [:th "total"] [:th "tags"]]])
  )

;; count backwards 30 days and render
(defn render [label label-events]
  (str (subs (format "%-40s " label) 0 40) "|"
       (clojure.string/join
        (map
         #(daterender % label-events)
         (datelist) ))
       ;;label-events ;; for occasional debug
       "|"
       " total: "
       (get  label-events "total")
       " tags: "
       (get  label-events "tags")
;;       label-events
       )  )

(defn render-tr [label label-events]
  (html [:tr  [:th (subs (format "%-40s " label) 0 40)]  
         
         (clojure.string/join  (map
                                #(daterender-td % label-events)
                                (datelist) ))
         [:td  (get  label-events "total")]
         [:td        (clojure.string/join "," (get  label-events "tags"))]

         ])  )

(defn write-reports [reportname events-compact-in]
  (let [txtreport (str "out/" reportname ".txt")
        htmlreport (str "out/" reportname ".html")
        ;; sort events-compact, by number of tags, then number of events.
        events-sorted (reverse (sort-by #(vector (count (get  (second %) "tags"))
                                                 (get  (second %) "total")
                                                 )          
                                        events-compact-in))
        ]
    (spit txtreport (str/join (map #(str (shared/render (first %) (second %)) "\n") events-sorted)))
    (println "wrote " txtreport)
    
    (spit htmlreport (str/join (map #(str (shared/render-tr (first %) (second %)) "\n") events-sorted)))
    (println "wrote " htmlreport))

  )
