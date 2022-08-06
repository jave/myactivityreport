.ONESHELL:

outfilestxt = out/gitlab-report.txt  out/filewatch-report.txt  out/agenda.txt out/filewatch-report.txt out/ff-report.txt
outfileshtml =  out/gitlab-report.html  out/dates.html out/agenda.html out/filewatch-report.html out/ff-report.html out/habit-report.html
htmlreport =  out/index.html
include settings.mk



all: report $(htmlreport)
clean:
	rm -f $(outfilestxt) $(outfileshtml) $(htmlreport)


report:   $(outfilestxt)
	echo $(outfilestxt)
	cat $(outfilestxt)

out/gitlab-report.txt out/gitlab-report.html &:
	./gle.clj

out/filewatch-report.txt out/filewatch-report.html &:
	./fsw2.clj

out/ff-report.txt out/ff-report.html &:
	cp $(PLACES_SQLITE) .
	./ff.clj

out/dates.html:
	./dates.clj

out/agenda.txt out/agenda.html &:
	./myhabits.el

out/habit-report.txt out/habit-report.html &: out/agenda.txt
	./myhabit.clj

$(htmlreport): index-head.html  empty.html  index-foot.html $(outfileshtml) style.css
	cp style.css out
	cat index-head.html out/dates.html out/habit-report.html out/gitlab-report.html empty.html out/filewatch-report.html empty.html out/ff-report.html index-foot.html out/agenda.html > $(htmlreport)
