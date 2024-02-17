.PHONY: build
build:
	mvn install

.PHONY: run
run:
	mvn javafx:run

.PHONY: watch
watch:
	find src -name "*.java" | entr -c make run

.PHONY: clean
clean:
	mvn clean
