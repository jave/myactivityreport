#!/bin/bash
#_(
   #_You can put other options here
   OPTS='
   -J-Xms256m -J-Xmx256m -J-client
   '
   . settings.mk
   echo exec clojure $OPTS "$0" "$@"
   exec clojure -X gle/-main $OPTS "$0" "$@"
   )

(ns gle)
(require '[java-time :as t])
(require '[shared])
(use 'hiccup.core)

(println "gitlabbing... ")

(def gitlab-token (System/getenv "GITLAB_TOKEN"))

(def api (org.gitlab4j.api.GitLabApi. "https://gitlab.com", gitlab-token))


(def events (.getAuthenticatedUserEvents (.getEventsApi api)
                                         nil nil ;; action target
                                         nil (t/java-date (t/minus (t/instant) (t/days shared/num-days) )) ;; before after
                                         nil ;;sortorder
                                         ))



(defn update-events-compact [events-compact event]
  (-> (update-in events-compact
                 ["gitlab-events" ;; 
                  (shared/date-to-key (.getCreatedAt event))]
                 #(if (nil? %) 1 (inc %)))
      (update-in  ["gitlab-events"
                   "total"
                   ]
                  #(if (nil? %) 1 (inc %))))
)



(def events-compact (-> (assoc-in (reduce update-events-compact {} events) ["gitlab-events" "tags"] ["0day"])
                        (shared/add-prio ,)
                        (shared/add-days , )))


(defn -main [& args]
  (shared/write-reports "gitlab-report" events-compact)
  )

