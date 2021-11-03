ADDITIONAL_MVN_ARGS ?= -DskipTests -q
export AWS_DEFAULT_REGION ?= us-east-1
export AWS_REGION ?= us-east-1
export AWS_ACCESS_KEY_ID ?= test
export AWS_SECRET_ACCESS_KEY ?= test
export SERVICES ?= serverless,kinesis,sns,sqs,iam,cloudwatch,qldb

usage:           ## Show this help
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'

build:           ## Build the code using Maven
	mvn -Pfatjar $(ADDITIONAL_MVN_ARGS) clean javadoc:jar source:jar package $(ADDITIONAL_MVN_TARGETS)
	mvn $(ADDITIONAL_MVN_ARGS) clean javadoc:jar source:jar package

compile:
	mvn $(ADDITIONAL_MVN_ARGS) -DskipTests compile test-compile

publish-maven:   ## Publish artifacts to Maven Central
	ADDITIONAL_MVN_TARGETS=deploy ADDITIONAL_MVN_ARGS="-DskipTests" make build

test-v1:
	mvn $(MVN_TEST_ARGS) -Dtest="cloud.localstack.awssdkv1.*Test" test

test-v2:
	mvn $(MVN_TEST_ARGS) -Dtest="cloud.localstack.awssdkv2.*Test" test

test:            ## Run Java/JUnit tests for AWS SDK v1 and v2
	make test-v2
	make test-v1

.PHONY: usage clean install test test-v1 test-v2
