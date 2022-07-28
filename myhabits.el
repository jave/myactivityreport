#!/usr/bin/emacs --script
;;; myhabits.el --- Description -*- lexical-binding: t; -*-
;;
;;; Commentary: extract org habits to a file, for further processing
;;
;;  Description
;;
;;; Code:

;; Setup
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
 (message "\n==== Installing depedencies ====")
(package-install 'htmlize)
;; (require 'org-id)
;; (require 'ox-hugo)

;; ;; Export content from org to Hugo md
;; (message "\n==== Exporting Hugo markdown ====")
;; (with-current-buffer (find-file "./Oblog.org") ;; JAVE
;; (org-hugo-export-wim-to-md :all-subtrees nil :visible-only nil))

;; (message "\n==== Export complete ====")
(require 'org-habit)
(require 'htmlize)
(setq org-agenda-span 'day)
(setq org-habit-graph-column 80)
(setq org-habit-preceding-days 30)
(setq org-habit-show-all-today t)
(setq org-agenda-files '("~/Plans"))
;;(org-agenda nil "o")
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
(org-agenda nil "a")

(with-current-buffer "*Org Agenda*"
  (write-region (point-min) (point-max) "out/agenda.txt")
  (with-current-buffer (htmlize-buffer)
      (write-region (point-min) (point-max) "out/agenda.html")
    )
  )


;;; myhabits.el ends here
