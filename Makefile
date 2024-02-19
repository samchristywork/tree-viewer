.PHONY: build
build:
	mvn package

start:
	java -jar target/Tasker-1.0.0.jar

.PHONY: run
run:
	mvn javafx:run
	find backups/ | sort | tail -n2 | xargs diff --color=always -u

.PHONY: watch
watch:
	find src -name "*.java" | entr -c make run

.PHONY: clean
clean:
	mvn clean
