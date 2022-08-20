#!/usr/bin/emacs --script
;;; nudges.el --- Description -*- lexical-binding: t; -*-
;;
;;; Commentary: extract org habits to a file, for further processing
;;
;;  Description
;;
;;; Code:

;; Setup
(setq max-lisp-eval-depth 6000)
(message "\n==== Setup package repos ====")
(require 'package)
(setq package-user-dir (expand-file-name "./.packages"))
(setq package-archives '(("melpa" . "https://melpa.org/packages/")
                         ("elpa" . "https://elpa.gnu.org/packages/")))

;; ;; Initialize the package system
(package-initialize)
(unless package-archive-contents
  (package-refresh-contents))

;; ;; Install and dependencies
;; this is not ultra-robust for melpa, because melpa might be on a rebuild when package-install happens, and then install might fail
;; happened during org-ql install
(message "\n==== Installing depedencies ====")
(package-install 'org-ql)
(package-install 'parseedn)


(require 'org-habit)
(require 'parseedn)
(require 'org-ql)
(setq org-agenda-span 'day)
(setq org-habit-graph-column 80)
(setq org-habit-preceding-days 30)
(setq org-habit-show-all-today t)
(setq org-agenda-files '("~/Plans"))
(defun jv-org-agenda-files ()
  "find my org files, must contain TODO!"
  (interactive)
  (setq org-agenda-files
        (remove ""
                (mapcar 'abbreviate-file-name
                        (split-string
                         (shell-command-to-string "find -L /home/joakim/Plans/  -regex '.*org' -and -not -name '#*' -and -not -name '.#*'    -exec grep  --files-with-matches  '* TODO' '{}' ';' ") "\n"))))
  )

(jv-org-agenda-files)


;; the rest of this file could go into a nudges.el file i guess
;; experiment org-ql

(defun find-nudge-dates()
  (let (
        (datehash (make-hash-table  :test 'equal)))
    (while (re-search-forward org-ts-regexp-inactive nil t)

      (puthash (org-format-time-string "%Y%m%d" (org-time-string-to-time (match-string-no-properties 1)))
               1 ;;one event this day... could be more, but never mind for now
	    datehash)
      )
    datehash))

    ;;hmm and then, for each line
    ;; save-excursion
    ;; org-agenda-switch-to
    ;; org-narrow-to-subtree
    ;; search for org-ts-regexp
    ;; save each ts to a hasmap

    ;; end result would be a event structure, that i can parse in the clojure parts
    ;; maybe use this: https://github.com/clojure-emacs/parseedn/blob/main/test/parseedn-test.el

(defun nudges-as-edn()
  (org-ql-search (org-agenda-files) '(and (todo) (tags "nudge")(level 1))  :buffer "nudges")
  (with-current-buffer "nudges"
    (write-region (point-min) (point-max) "out/nudges.txt")
    (let ((events (make-hash-table :test 'equal))
          ;;(heading "")
          ;;(dates nil)
          )
      
      (while (not (eobp))
        (save-excursion
          (org-agenda-switch-to)
          (org-narrow-to-subtree)
          (let ((heading (substring-no-properties (org-get-heading t t t t) ;; atm i only want the heading, no tags etc, maybe later
                                                  ))
                (tags (org-get-tags))
                (nudge-dates (find-nudge-dates)))
            ;; (message "heading:%s" heading)
            ;; (message "dates: %s" nudge-dates)
            ;;(puthash heading nudge-dates events)
            ;;(puthash heading (puthash "tags" '("demotag" "othertag") (make-hash-table :test 'equal)) events)
            (puthash "tags" tags nudge-dates)
            (puthash heading nudge-dates  events)
            )  
          )
        
        (forward-line 1))
              (parseedn-print-str events)
      )
        
    
    ))

(with-temp-file "out/nudges.edn"
  (insert (nudges-as-edn))
  )
;;; myhabits.el ends here
