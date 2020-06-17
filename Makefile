ADDITIONAL_MVN_ARGS ?= -DskipTests -q

usage:           ## Show this help
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'

build:           ## Build the code using Maven
	mvn -Pfatjar $(ADDITIONAL_MVN_ARGS) clean javadoc:jar source:jar package $(ADDITIONAL_MVN_TARGETS)

publish-maven:   ## Publish artifacts to Maven Central
	ADDITIONAL_MVN_TARGETS=deploy ADDITIONAL_MVN_ARGS=" " make build

test:            ## Run Java/JUnit tests for AWS SDK v1 and v2
	USE_SSL=1 SERVICES=serverless,kinesis,sns,sqs,iam,cloudwatch mvn -Pawssdkv2 \
		-Dtest="cloud.localstack.awssdkv2.*Test" test
	USE_SSL=1 SERVICES=serverless,kinesis,sns,sqs,iam,cloudwatch mvn -Pawssdkv1 \
		-Dtest="!cloud.localstack.awssdkv2.*Test" test

.PHONY: usage clean install test
