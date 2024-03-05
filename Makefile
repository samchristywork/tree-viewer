.PHONY: run
run:
	mvn javafx:run

.PHONY: build
build:
	mvn package

.PHONY: package
package:
	mvn package

.PHONY: start
start:
	java -jar target/Tasker-1.0.0.jar

.PHONY: watch
watch:
	find src -name "*.java" | entr -c make run

.PHONY: clean
clean:
	mvn clean
