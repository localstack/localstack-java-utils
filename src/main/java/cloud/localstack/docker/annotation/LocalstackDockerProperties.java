package cloud.localstack.docker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An annotation to provide parameters to the main (Docker-based) LocalstackTestRunner
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface LocalstackDockerProperties {

    /**
     * Used for determining the host name of the machine running the docker containers
     * so that the containers can be addressed.
     */
    Class<? extends IHostNameResolver> hostNameResolver() default LocalHostNameResolver.class;

    /**
     * Used for injecting environment variables into the container.  Implement a class that provides a map of the environment
     * variables and they will be injected into the container on start-up
     */
    Class<? extends IEnvironmentVariableProvider> environmentVariableProvider() default DefaultEnvironmentVariableProvider.class;

    /**
     * Used for injecting directories or files that are bind mounted into the docker container. Implement a class that provides a map
     * of host to container directory or file mappings, with host directories/files as keys and container directories/files as values.
     * Remember that Docker needs absolute paths for host directories and files.
     */
    Class<? extends IBindMountProvider> bindMountProvider() default IBindMountProvider.EmptyBindMountProvider.class;

    /**
     * A Java Regex that is used to wait for the execution of custom init scripts 
     */
    String initializationToken() default "";

    /**
     * Determines if a new image is pulled from the docker repo before the tests are run.
     */
    boolean pullNewImage() default false;

    /**
     * Determines if the container should expose the default local stack ports (4567-4583) or if it should expose randomized ports
     *  in order to prevent conflicts with other localstack containers running on the same machine.
     *
     * Deprecated, since latest LocalStack is using a single entry point (edge service)
     */
    @Deprecated
    boolean randomizePorts() default false;

    /**
     * Determines which services should be run when the localstack starts. When empty, all the services available get
     * up and running.
     * @see cloud.localstack.ServiceName
     */
    String[] services() default {};

    /**
     * Use a specific image name (consisting of organisation and repository, e.g. localstack/localstack-full for docker container
     */
    String imageName() default "";

    /**
     * Use a specific image tag for docker container
     */
    String imageTag() default "";

    /**
     * Port number for the edge service, the main entry point for all API invocations. Alternatively, use the
     * LOCALSTACK_EDGE_PORT environment variable.
     */
    String portEdge() default "4566";

    /**
     * Port number for the elasticsearch service. Alternatively, use the LOCALSTACK_ELASTICSEARCH_PORT environment
     * variable.
     */
    String portElasticSearch() default "4571";

    /**
     * Determines if the singleton container should be used by all test classes
     */
    boolean useSingleDockerContainer() default false;

    /**
     * Determines if errors should be ignored when starting the Docker container.
     * This can be used to run tests with an existing LocalStack container running on the host.
     */
    boolean ignoreDockerRunErrors() default false;

    /**
     * Specifies a target platform for the localstack docker image. Value is used by the --platform flag in the
     * docker run command
     */
    String platform() default "";
}
