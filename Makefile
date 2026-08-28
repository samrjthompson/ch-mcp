.PHONY: clean format format-check unit-test test

clean:
	mvn clean

format: clean
	mvn spotless:apply

format-check: clean
	mvn spotless:check

unit-test: clean format-check
	mvn test

test: clean format-check
	mvn verify
