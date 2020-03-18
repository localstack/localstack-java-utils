ADDITIONAL_MVN_ARGS ?= -DskipTests -q -X

usage:           ## Show this help
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'

build:           ## Build the code using Maven
	mvn -Pfatjar $(ADDITIONAL_MVN_ARGS) clean javadoc:jar source:jar package $(ADDITIONAL_MVN_TARGETS)

publish-maven:   ## Publish artifacts to Maven Central
	ADDITIONAL_MVN_TARGETS=deploy ADDITIONAL_MVN_ARGS=" " make build

test:            ## Run tests for Java/JUnit compatibility
	USE_SSL=1 SERVICES=serverless,kinesis,sns,sqs,cloudwatch mvn $(MVN_ARGS) -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent -Djava.compiler=NONE" test

.PHONY: usage clean install test
