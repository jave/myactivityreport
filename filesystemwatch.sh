#!/bin/sh
echo "starting monitoring..."
date

# these are excluded because i want to monitor stuff i do, not what random systems implementation details do
EXCLUDE=".mozilla" #mozilla creates a lot of stuff
EXCLUDE+="|.cache|.git|.config|.unison" # some application workdirs
EXCLUDE+="|.local/share/containers" # podman containers
EXCLUDE+="|.#" # .# files are emacs lock files
EXCLUDE+="|navidrome.db-wal" #navidrome random file
EXCLUDE+="|/flib/[music]/navidromedata/navidrome.db"
# EXCLUDE+="|.emacs.d/auto-save-list/" #emacs auto saves
# EXCLUDE+="|.emacs.keyfreq" #emacs file
# EXCLUDE+="|.emacs.desktop" #emacs file
# EXCLUDE+=".emacs.d/projectile-bookmarks.eld"
EXCLUDE+="|.emacs" # no .emacs files, for now
EXCLUDE+="|projectile-bookmarks.eld"
EXCLUDE+="|infra/docker/mariewp/" # mariewp generates wordpress noice, im kinda reluctant not to monitor it...
EXCLUDE+="|.syncthing" #syncthing temp files
EXCLUDE+="|~$" #ending with tilde emacs backups, and also krita backups
EXCLUDE+="|.newsrc" # gnus files, emacs makes a lot of files...
EXCLUDE+="|.vnc" # turbovnc
EXCLUDE+="|.x2go" # x2go
EXCLUDE+="|.local/state/" #wireplumber
EXCLUDE+="|/home/joakim/media" # not sure why this pops up
EXCLUDE+="|Plans/bookmarks" # seems to be written to a lot
EXCLUDE+="|.autosave.xopp"
EXCLUDE+="|.vagrant.d" 
EXCLUDE+="|filesystemwatch" # otherwise i guess recursion
EXCLUDE+="|myactivityreport" # otherwise i guess recursion
echo "skipping $EXCLUDE"

# events to listen for
EVENTS="modify,attrib,move,close_write,create,delete,delete_self"
# i want some kind of log format that is easy to read and parse, and standard
# - %FT%TZ -> 2022-06-09T23:38:20Z, iso 8601 hopefully
# - file names can contain spaces or other weirdness, atm i use " | " as field separator, lets hope no filename contains that
inotifywait --timefmt "%FT%TZ" --format "%T | %w%f | %e" \
            --exclude $EXCLUDE \
            -rme $EVENTS  $HOME $HOME/roles /flib/ >>filesystemwatch.log &

tail -n 100 -f filesystemwatch.log
