[![Build Status](https://travis-ci.org/localstack/localstack-java-utils.svg)](https://travis-ci.org/localstack/localstack-java-utils)

# LocalStack Java Utils

Java utilities and JUnit integration for [LocalStack](https://github.com/localstack/localstack).

## Prerequisites

* Java
* Maven
* Docker
* LocalStack

## Usage

In order to use LocalStack with Java, this project provides a simple JUnit runner and a JUnit 5
extension. Take a look at the example JUnit tests in `src/test/java`.

By default, the JUnit Test Runner starts LocalStack in a Docker container, for the duration of the test.
The container can be configured by using the `@LocalstackDockerProperties` annotation.

```
...
import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.TestUtils;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;

@RunWith(LocalstackTestRunner.class)
@LocalstackDockerProperties(services = { "s3", "sqs", "kinesis" })
public class MyCloudAppTest {

  @Test
  public void testLocalS3API() {
    AmazonS3 s3 = TestUtils.getClientS3()
    List<Bucket> buckets = s3.listBuckets();
    ...
  }

}
```

Or with JUnit 5:

```
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(...)
public class MyCloudAppTest {
   ...
}
```

## Installation

The LocalStack JUnit test runner is published as an artifact in Maven Central.
Simply add the following dependency to your `pom.xml` file:

```
<dependency>
    <groupId>cloud.localstack</groupId>
    <artifactId>localstack-utils</artifactId>
    <version>0.2.2</version>
</dependency>
```

## Configuration

You can configure the Docker behaviour using the `@LocalstackDockerProperties` annotation with the following parameters:

| property                    | usage                                                                                                                        | type                         | default value |
|-----------------------------|------------------------------------------------------------------------------------------------------------------------------|------------------------------|---------------|
| `pullNewImage`              | Determines if a new image is pulled from the docker repo before the tests are run.                                           | boolean                      | `false`         |
| `services`                  | Determines which services should be run when the localstack starts.                                                          | String[]                     | All           |
| `imageTag`                  | Use a specific image tag for docker container                                                                                | String                       | `latest`        |
| `hostNameResolver`          | Used for determining the host name of the machine running the docker containers so that the containers can be addressed.     | IHostNameResolver            | `localhost`     |
| `environmentVariableProvider` | Used for injecting environment variables into the container.                                                                 | IEnvironmentVariableProvider | Empty Map     |
| `useSingleDockerContainer`  | Whether a singleton container should be used by all test classes.                     | boolean | `false`     |

For more details, please refer to the README of the main LocalStack repo: https://github.com/localstack/localstack

> **_NOTE:_** These utilities assume docker is installed in one of the default locations (`C:\program files\docker\docker\resources\bin\docker.exe`,
`C:\program files\docker\docker\resources\docker.exe`, `usr/local/bin/docker` or `usr/bin/docker`). If your docker executable is in a
different location, then use the `DOCKER_LOCATION` environment variable to specify it.

### Deprecated Configurations

Due to recent changes in LocalStack (exposing all services via a single edge port, `4566`), the following configuration parameters are now deprecated in the latest version:

| property                    | usage                                                                                                                        | type                         | default value |
|-----------------------------|------------------------------------------------------------------------------------------------------------------------------|------------------------------|---------------|
| `randomizePorts`            | Determines if the container should expose the default local stack ports (4567-4583) or if it should expose randomized ports. | boolean                      | `false`         |

_Note: When specifying the port in the `services` property, you cannot use `randomizePorts = true`_

## Building

To build the latest version of the code via Maven:
```
make build
```

## Change Log

* v0.2.2: Addition of CloudWatch Logs endpoint configuration; adjust tests to use central edge service endpoint
* v0.2.1: Move Java sources into separate project; mark non-Docker Java `LocalstackExtension` as deprecated; update paths for Python code lookup in Docker container
* v0.2.0: Last version still maintained in LocalStack main repo

## License

This code is released under the Apache License, Version 2.0 (see LICENSE.txt).
