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
;; this is not ultra-robust for melpa, because melpa might be on a rebuild when package-install happens, and then install might fail
;; happened during org-ql install
(message "\n==== Installing depedencies ====")
(package-install 'htmlize)


(require 'org-habit)
(require 'htmlize)
(require 'parseedn)
(require 'org-ql)
(setq org-agenda-span 'day)
(setq org-habit-graph-column 80)
(setq org-habit-preceding-days 30)
(setq org-habit-show-all-today t)
(setq org-agenda-files '("~/Plans"))
;;(org-agenda nil "o")
(setq org-agenda-prefix-format
      '((agenda . " %i %-20:c%?-12t% s")
        (timeline . "  % s")
        (todo . " %i %3(jv-todoinfo)| %-20:c")        
        (tags . " %i %-12:c")
        (search . " %i %-12:c"))
      )
(setq org-agenda-tags-column -200)
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

(read-only-mode nil)
(let ((inhibit-read-only t))
  (goto-char (point-min))
  ;; this loop aims to delete everything that isnt a habit, because i couldnt figure out how to configure the agenda
  (while (not (eobp))
    (beginning-of-line)
    (if (not (get-text-property (point) 'org-habit-p))
        (progn 
          (kill-line)
	  (kill-line))
      (forward-line 1)))
  (write-region (point-min) (point-max) "out/agenda.txt")
  (with-current-buffer (htmlize-buffer)
    (write-region (point-min) (point-max) "out/agenda.html")
    ))

;;; myhabits.el ends here
