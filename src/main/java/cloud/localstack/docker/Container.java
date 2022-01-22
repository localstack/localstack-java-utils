package cloud.localstack.docker;

import cloud.localstack.Localstack;
import cloud.localstack.docker.command.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * An abstraction of the LocalStack docker container. Provides port mappings, a way
 * to poll the logs until a specified token appears, and the ability to stop the container.
 */
public class Container {

    private static final Logger LOG = Logger.getLogger(Container.class.getName());

    private static final String LOCALSTACK_NAME = "localstack/localstack";
    private static final String LOCALSTACK_TAG = "latest";
    private static final String LOCALSTACK_PORT_EDGE = "4566";
    private static final String LOCALSTACK_PORT_ELASTICSEARCH = "4571";

    private static final int MAX_PORT_CONNECTION_ATTEMPTS = 10;
    private static final int MAX_LOG_COLLECTION_ATTEMPTS = 120;
    private static final long POLL_INTERVAL = 1000;
    private static final int NUM_LOG_LINES = 1000;

    private static final String ENV_DEBUG = "DEBUG";
    private static final String ENV_USE_SSL = "USE_SSL";
    private static final String ENV_DEBUG_DEFAULT = "1";
    public static final String LOCALSTACK_EXTERNAL_HOSTNAME = "HOSTNAME_EXTERNAL";

    private static final String DEFAULT_CONTAINER_ID = "localstack_main";

    private final String containerId;
    private final List<PortMapping> ports;

    private boolean startedByUs;

    /**
     * It creates a container using the hostname given and the set of environment variables provided
     * @param externalHostName hostname to be used by localstack
     * @param pullNewImage determines if docker pull should be run to update to the latest image of the container
     * @param randomizePorts determines if the container should expose the default local stack ports or if it should expose randomized ports
     *                       in order to prevent conflicts with other localstack containers running on the same machine
     * @param imageName the name of the image defaults to {@value LOCALSTACK_NAME} if null
     * @param imageTag the tag of the image to pull, defaults to {@value LOCALSTACK_TAG} if null
     * @param environmentVariables map of environment variables to be passed to the docker container
     * @param portMappings
     * @param bindMounts  Docker host to container volume mapping like /host/dir:/container/dir, be aware that the host
     * directory must be an absolute path
     * @param platform target platform for the localstack docker image
     */
    public static Container createLocalstackContainer(
            String externalHostName, boolean pullNewImage, boolean randomizePorts, String imageName, String imageTag, String portEdge,
            String portElasticSearch, Map<String, String> environmentVariables, Map<Integer, Integer> portMappings,
            Map<String, String> bindMounts, String platform) {

        environmentVariables = environmentVariables == null ? Collections.emptyMap() : environmentVariables;
        bindMounts = bindMounts == null ? Collections.emptyMap() : bindMounts;
        portMappings = portMappings == null ? Collections.emptyMap() : portMappings;

        String imageNameOrDefault = (imageName == null ? LOCALSTACK_NAME : imageName);
        String fullImageName = imageNameOrDefault + ":" + (imageTag == null ? LOCALSTACK_TAG : imageTag);
        boolean imageExists = new ListImagesCommand().execute().contains(fullImageName);

        String fullPortEdge = (portEdge == null ? LOCALSTACK_PORT_EDGE : portEdge) + ":" + LOCALSTACK_PORT_EDGE;
        String fullPortElasticSearch = (portElasticSearch == null ? LOCALSTACK_PORT_ELASTICSEARCH : portElasticSearch)
            + ":" + LOCALSTACK_PORT_ELASTICSEARCH;

        if(pullNewImage || !imageExists) {
            LOG.info(String.format("Pulling image %s", fullImageName));
            new PullCommand(imageNameOrDefault, imageTag).execute();
        }

        RunCommand runCommand = new RunCommand(imageNameOrDefault, imageTag)
            .withExposedPorts(fullPortEdge, randomizePorts)
            .withExposedPorts(fullPortElasticSearch, randomizePorts)
            .withEnvironmentVariable(LOCALSTACK_EXTERNAL_HOSTNAME, externalHostName)
            .withEnvironmentVariable(ENV_DEBUG, ENV_DEBUG_DEFAULT)
            .withEnvironmentVariable(ENV_USE_SSL, Localstack.useSSL() ? "1" : "0")
            .withEnvironmentVariables(environmentVariables)
            .withBindMountedVolumes(bindMounts);

        if(!StringUtils.isEmpty(platform))
            runCommand = runCommand.withPlatform(platform);

        for (Integer port : portMappings.keySet()) {
            runCommand = runCommand.withExposedPorts(String.valueOf(port), false);
        }
        String containerId = runCommand.execute();
        LOG.info("Started container: " + containerId);

        Container result = getRunningLocalstackContainer(containerId);
        result.startedByUs = true;
        return result;
    }

    public static Container getRunningLocalstackContainer() {
        return getRunningLocalstackContainer(DEFAULT_CONTAINER_ID);
    }

    public static Container getRunningLocalstackContainer(String containerId) {
        List<PortMapping> portMappingsList = new PortCommand(containerId).execute();
        return new Container(containerId, portMappingsList);
    }

    private Container(String containerId, List<PortMapping> ports) {
        this.containerId = containerId;
        this.ports = Collections.unmodifiableList(ports);
    }

    /**
     * Given an internal port, retrieve the publicly addressable port that maps to it
     */
    public int getExternalPortFor(int internalPort) {
        Integer externalPort = ports.stream()
                .filter(port -> port.getInternalPort() == internalPort)
                .map(PortMapping::getExternalPort)
                .findFirst().orElse(null);

        if (externalPort != null) {
            return externalPort;
        }
        if (internalPort == Localstack.DEFAULT_EDGE_PORT) {
            return internalPort;
        }

        throw new IllegalArgumentException("Port " + internalPort + " is not mapped in the LocalStack container");
    }

    public void waitForAllPorts(String ip) {
        ports.forEach(port -> waitForPort(ip, port));
    }

    private void waitForPort(String ip, PortMapping port) {
        int attempts = 0;
        do {
            if(isPortOpen(ip, port)) {
                return;
            }
            attempts++;
        }
        while(attempts < MAX_PORT_CONNECTION_ATTEMPTS);

        throw new IllegalStateException("Could not open port:" + port.getExternalPort() + " on ip:" + port.getIp());
    }

    private boolean isPortOpen(String ip, PortMapping port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port.getExternalPort()), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isRunning() {
        try {
            new PortCommand(containerId).execute();
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    /**
     * Poll the docker logs until a specific token appears, then return. Primarily
     * used to look for the "Ready." token in the LocalStack logs.
     */
    public void waitForLogToken(Pattern pattern) {
        int attempts = 0;
        do {
            if(logContainsPattern(pattern)) {
                return;
            }
            waitForLogs();
            attempts++;
        }
        while(attempts < MAX_LOG_COLLECTION_ATTEMPTS);

        String logs = getContainerLogs();
        throw new IllegalStateException("Could not find token: " + pattern + " in Docker logs: " + logs);
    }

    private boolean logContainsPattern(Pattern pattern) {
        String logs = getContainerLogs();
        return pattern.matcher(logs).find();
    }

    private String getContainerLogs() {
        return new LogCommand(containerId).withNumberOfLines(NUM_LOG_LINES).execute();
    }

    private void waitForLogs(){
        try {
            Thread.sleep(POLL_INTERVAL);
        }
        catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Stop the container
     */
    public void stop() {
        if (!startedByUs) {
            return;
        }
        new StopCommand(containerId).execute();
        LOG.info("Stopped container: " + containerId);
    }

    /**
     * Run a command on the container via docker exec
     */
    public String executeCommand(List<String> command) {
        return new ExecCommand(containerId).execute(command);
    }

    /**
     * Returns the container ID which can be used to execute Docker CLI / API level commands on the container.
     */
    String getContainerId() {
        return containerId;
    }
}
