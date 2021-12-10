package cloud.localstack;

import cloud.localstack.docker.*;
import cloud.localstack.docker.command.*;
import cloud.localstack.docker.annotation.LocalstackDockerConfiguration;
import cloud.localstack.docker.exception.LocalstackDockerException;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Localstack Docker instance
 *
 * @author Alan Bevier
 * @author fabianoo
 */
public class Localstack {

    public static final String ENV_CONFIG_USE_SSL = "USE_SSL";
    public static final String ENV_CONFIG_EDGE_PORT = "EDGE_PORT";
    public static final String INIT_SCRIPTS_PATH = "/docker-entrypoint-initaws.d";
    public static final String TMP_PATH = "/tmp/localstack";
    public static final int DEFAULT_EDGE_PORT = 4566;

    private static final Logger LOG = Logger.getLogger(Localstack.class.getName());

    private static final Pattern READY_TOKEN = Pattern.compile("Ready\\.");

    private static final String[] PYTHON_VERSIONS_FOLDERS = { "python3.8", "python3.7" };

    private static final String PORT_CONFIG_FILENAME = "/opt/code/localstack/"
            + ".venv/lib/%s/site-packages/localstack_client/config.py";

    // Regular expression used to parse localstack config to determine default ports
    // for services
    private static final Pattern DEFAULT_PORT_PATTERN = Pattern.compile("'(\\w+)'\\Q: '{proto}://{host}:\\E(\\d+)'");

    private Container localStackContainer;

    // Whether to use the edge port 4566 as fallback if the service port cannot be determined
    private boolean useEdgePortAsFallback = true;

    /**
     * This is a mapping from service name to internal ports. In order to use them,
     * the internal port must be resolved to an external docker port via
     * Container.getExternalPortFor()
     */
    private static Map<String, Integer> serviceToPortMap;

    private static boolean locked = false;

    public static final Localstack INSTANCE = new Localstack();

    private String externalHostName;

    static {
        // make sure we avoid any errors related to locally generated SSL certificates
        CommonUtils.disableSslCertChecking();
    }

    private Localstack() {}

    public void startup(LocalstackDockerConfiguration dockerConfiguration) {
        if (locked) {
            throw new IllegalStateException("A docker instance is starting or already started.");
        }
        locked = true;
        this.externalHostName = dockerConfiguration.getExternalHostName();

        Map<String, String> environmentVariables = dockerConfiguration.getEnvironmentVariables();
        environmentVariables = environmentVariables == null ? Collections.emptyMap() : environmentVariables;
        environmentVariables = new HashMap<String, String>(environmentVariables);
        // add default environment variables
        Map<String, String> defaultEnvVars = getDefaultEnvironmentVariables();
        environmentVariables.putAll(defaultEnvVars);

        try {
            localStackContainer = Container.createLocalstackContainer(dockerConfiguration.getExternalHostName(),
                    dockerConfiguration.isPullNewImage(), dockerConfiguration.isRandomizePorts(),
                    dockerConfiguration.getImageName(), dockerConfiguration.getImageTag(),
                    dockerConfiguration.getPortEdge(), dockerConfiguration.getPortElasticSearch(),
                    environmentVariables, dockerConfiguration.getPortMappings(),
                    dockerConfiguration.getBindMounts(), dockerConfiguration.getPlatform());
            loadServiceToPortMap();

            LOG.info("Waiting for LocalStack container to be ready...");
            localStackContainer.waitForLogToken(READY_TOKEN);
            if (dockerConfiguration.getInitializationToken() != null) {
                LOG.info("Waiting for LocalStack container to emit your initialization token '"
                        + dockerConfiguration.getInitializationToken().toString() + "'...");
                localStackContainer.waitForLogToken(dockerConfiguration.getInitializationToken());
            }
        } catch (Exception t) {
            if ((t.toString().contains("port is already allocated") || t.toString().contains("address already in use")) 
                && dockerConfiguration.isIgnoreDockerRunErrors()) {
                LOG.info("Ignoring port conflict when starting Docker container, due to ignoreDockerRunErrors=true");
                localStackContainer = Container.getRunningLocalstackContainer();
                loadServiceToPortMap();
                return;
            }
            this.stop();
            throw new LocalstackDockerException("Could not start the localstack docker container.", t);
        }
    }

    public void stop() {
        if (localStackContainer != null) {
            localStackContainer.stop();
            localStackContainer = null;
        }
        locked = false;
    }

    public boolean isRunning() {
        return localStackContainer == null ? false : localStackContainer.isRunning();
    }

    private void loadServiceToPortMap() {
        try {
            doLoadServiceToPortMap();
        } catch (Exception e) {
            LOG.info("Ignoring error when fetching service ports -> using single edge port");
        }
    }

    private Map<String, String> getDefaultEnvironmentVariables() {
        Map<String, String> result = new HashMap<String, String>();
        addEnvVariableIfDefined(Constants.ENV_LOCALSTACK_API_KEY, result);
        return result;
    }

    private void addEnvVariableIfDefined(String envVarName, Map<String, String> envVars) {
        String value = System.getenv(envVarName);
        if (value != null) {
            envVars.put(envVarName, value);
        }
    }

    // TODO: this is now obsolete, as we're using a single edge port - remove!
    private void doLoadServiceToPortMap() {
        String localStackPortConfig = "";
        for (int i = 0; i < PYTHON_VERSIONS_FOLDERS.length; i++) {
            String filePath = String.format(PORT_CONFIG_FILENAME, PYTHON_VERSIONS_FOLDERS[i]);
            
            localStackPortConfig = localStackContainer.executeCommand(Arrays.asList("cat", filePath));
            if (localStackPortConfig.contains("No such container")) {
                localStackPortConfig = "";
                continue;
            } else if(localStackPortConfig.contains("No such file")) {
                localStackPortConfig = "";
                continue;
            } else {
                break;
            }
        }

        if (localStackPortConfig.isEmpty()) {
            throw new LocalstackDockerException("No config file found",new Exception());
        }

        int edgePort = getEdgePort();
        Map<String, Integer> ports = new RegexStream(DEFAULT_PORT_PATTERN.matcher(localStackPortConfig)).stream()
                .collect(Collectors.toMap(match -> match.group(1), match -> edgePort));

        serviceToPortMap = Collections.unmodifiableMap(ports);
    }

    public String getEndpointS3() {
        String s3Endpoint = endpointForService(ServiceName.S3);
        /*
         * Use the domain name wildcard *.localhost.localstack.cloud which maps to
         * 127.0.0.1 We need to do this because S3 SDKs attempt to access a domain
         * <bucket-name>.<service-host-name> which by default would result in
         * <bucket-name>.localhost, but that name cannot be resolved (unless hardcoded
         * in /etc/hosts)
         */
        s3Endpoint = s3Endpoint.replace("localhost", Constants.LOCALHOST_DOMAIN_NAME);
        return s3Endpoint;
    }

    public int getEdgePort() {
        String envEdgePort = System.getenv(ENV_CONFIG_EDGE_PORT);
        return envEdgePort == null ? DEFAULT_EDGE_PORT : Integer.parseInt(envEdgePort);
    }

    public String getEndpointKinesis() {
        return endpointForService(ServiceName.KINESIS);
    }

    public String getEndpointKMS() {
        return endpointForService(ServiceName.KMS);
    }

    public String getEndpointLambda() {
        return endpointForService(ServiceName.LAMBDA);
    }

    public String getEndpointDynamoDB() {
        return endpointForService(ServiceName.DYNAMO);
    }

    public String getEndpointDynamoDBStreams() {
        return endpointForService(ServiceName.DYNAMO_STREAMS);
    }

    public String getEndpointAPIGateway() {
        return endpointForService(ServiceName.API_GATEWAY);
    }

    public String getEndpointElasticsearch() {
        return endpointForService(ServiceName.ELASTICSEARCH);
    }

    public String getEndpointElasticsearchService() {
        return endpointForService(ServiceName.ELASTICSEARCH_SERVICE);
    }

    public String getEndpointFirehose() {
        return endpointForService(ServiceName.FIREHOSE);
    }

    public String getEndpointSNS() {
        return endpointForService(ServiceName.SNS);
    }

    public String getEndpointSQS() {
        return endpointForService(ServiceName.SQS);
    }

    public String getEndpointRedshift() {
        return endpointForService(ServiceName.REDSHIFT);
    }

    public String getEndpointCloudWatch() {
        return endpointForService(ServiceName.CLOUDWATCH);
    }

    public String getEndpointCloudWatchLogs() {
        return endpointForService(ServiceName.CLOUDWATCH_LOGS);
    }

    public String getEndpointSES() {
        return endpointForService(ServiceName.SES);
    }

    public String getEndpointRoute53() {
        return endpointForService(ServiceName.ROUTE53);
    }

    public String getEndpointCloudFormation() {
        return endpointForService(ServiceName.CLOUDFORMATION);
    }

    public String getEndpointSSM() {
        return endpointForService(ServiceName.SSM);
    }

    public String getEndpointSecretsmanager() {
        return endpointForService(ServiceName.SECRETSMANAGER);
    }

    public String getEndpointEC2() {
        return endpointForService(ServiceName.EC2);
    }

    public String getEndpointStepFunctions() {
        return endpointForService(ServiceName.STEPFUNCTIONS);
    }

    public String getEndpointIAM() {
        return endpointForService(ServiceName.IAM);
    }

    public String getEndpointQLDB() {
        return endpointForService(ServiceName.QLDB);
    }

    public String endpointForService(String serviceName) {
        return endpointForPort(getServicePort(serviceName));
    }

    public int getServicePort(String serviceName) {
        if (serviceToPortMap == null) {
            if (useEdgePortAsFallback) {
                return getEdgePort();
            }
            throw new IllegalStateException("Service to port mapping has not been determined yet.");
        }

        if (!serviceToPortMap.containsKey(serviceName)) {
            if (useEdgePortAsFallback) {
                return getEdgePort();
            }
            throw new IllegalArgumentException("Unknown port mapping for service: " + serviceName);
        }

        return serviceToPortMap.get(serviceName);
    }

    public String endpointForPort(int port) {
        if (localStackContainer != null) {
            int externalPort = localStackContainer.getExternalPortFor(port);
            String protocol = useSSL() ? "https" : "http";
            return String.format("%s://%s:%s", protocol, externalHostName, externalPort);
        }

        throw new RuntimeException("Container not started");
    }

    public Container getLocalStackContainer() {
        return localStackContainer;
    }

    public static boolean useSSL() {
        return isEnvConfigSet(ENV_CONFIG_USE_SSL);
    }

    public static boolean isEnvConfigSet(String configName) {
        String value = System.getenv(configName);
        return value != null && !Arrays.asList("false", "0", "").contains(value.trim());
    }

    public static String getDefaultRegion() {
        return Constants.DEFAULT_REGION;
    }
}
