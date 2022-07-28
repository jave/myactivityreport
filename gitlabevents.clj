#!/bin/bash
#_(
    #_ https://github.com/jafingerhut/dotfiles/blob/master/templates/clj-template-README.md
    #_DEPS is same format as deps.edn. Multiline is okay.
    DEPS='
    {:deps {org.gitlab4j/gitlab4j-api {:mvn/version "5.0.1"}}}
    '
 
    #_You can put other options here
    OPTS='
    -J-Xms256m -J-Xmx256m -J-client
   '
 . settings.sh
 echo clojure $OPTS -Sdeps "$DEPS" "$0" "$@"
 exec clojure $OPTS -Sdeps "$DEPS" "$0" "$@"
 
)

(println "gitlabbing... ")
 
(println)




;; ok these are not let-binded atm, but this is more like a shell script. im thinking about it
(def api (org.gitlab4j.api.GitLabApi. "https://gitlab.com", (System/getenv "GITLAB_TOKEN")))
(def events (.getAuthenticatedUserEvents (.getEventsApi api) nil nil nil nil nil))
(require '[java-time :as t])
;;(require '[java-time])

;; ;;dates of events
;; (take 10 (map #(.getCreatedAt  %) events))
;; ;; dates of events, truncated to days
;; (take 10 (map #(t/truncate-to (t/instant (.getCreatedAt  %)) :days) events))


;; works as a counter of events for a dates
;;(def events-compact (update events-compact  (t/truncate-to (t/instant ) :days) #(if (nil? %) 1 (inc %))))

(defn update-events-compact [events-compact event]

  (update events-compact  (t/truncate-to (t/instant (.getCreatedAt  event)) :days) #(if (nil? %) 1 (inc %)))
  )

(def events-compact (reduce update-events-compact {} events) )


;; so, this now reduces events to a list of dates, and the number of events per date
;; (reduce update-events-compact {} events)
;; (sort-by #(first %) (reduce update-events-compact {} events))
;;(take 10 (reverse (sort-by #(first %) (reduce update-events-compact {} events))))

;;( map #(t/format "YYY-mm-dd" (t/zoned-date-time (first %) (t/zone-id))) sorted)

;;( map #(list (t/format "YYY-mm-dd" (t/zoned-date-time (first %) (t/zone-id))) (second %)) sorted)

;;(take 3 (iterate minus (local-date) (t/days 1)))

  (println (take 30    (map #(get events-compact %)  (t/iterate t/minus (t/truncate-to (t/instant) :days) (t/days 1)) )  ))
