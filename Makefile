build:
	sudo rm -rf .cpcache build/ classes/
	mkdir classes
	clj -M:aot
	clj -M:uberdeps
