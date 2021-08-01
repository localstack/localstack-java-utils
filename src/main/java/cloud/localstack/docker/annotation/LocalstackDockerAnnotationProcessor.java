package cloud.localstack.docker.annotation;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Processor to retrieve docker configuration based on {@link LocalstackDockerProperties} annotation.
 *
 * @author Alan Bevier
 * @author Patrick Allain
 * @author Omar Khammassi
 */
public class LocalstackDockerAnnotationProcessor {

    private static final Logger LOG = Logger.getLogger(LocalstackDockerAnnotationProcessor.class.getName());

    public LocalstackDockerConfiguration process(final Class<?> klass) {
        return Stream.of(klass.getAnnotations())
            .filter(annotation -> annotation instanceof LocalstackDockerProperties)
            .map(LocalstackDockerProperties.class::cast)
            .map(this::processDockerPropertiesAnnotation)
            .findFirst()
            .orElse(LocalstackDockerConfiguration.DEFAULT);
    }

    private LocalstackDockerConfiguration processDockerPropertiesAnnotation(LocalstackDockerProperties properties) {
        return LocalstackDockerConfiguration.builder()
            .environmentVariables(this.getEnvironments(properties))
            .bindMounts(this.getBindMounts(properties))
            .initializationToken(StringUtils.isEmpty(properties.initializationToken()) ? null : Pattern.compile(properties.initializationToken()))
            .externalHostName(this.getExternalHostName(properties))
            .portMappings(this.getCustomPortMappings(properties))
            .pullNewImage(properties.pullNewImage())
            .ignoreDockerRunErrors(properties.ignoreDockerRunErrors())
            .randomizePorts(properties.randomizePorts())
            .imageName(StringUtils.isEmpty(properties.imageName()) ? null : properties.imageName())
            .imageTag(StringUtils.isEmpty(properties.imageTag()) ? null : properties.imageTag())
            .portEdge(getEnvOrDefault("LOCALSTACK_EDGE_PORT", properties.portEdge()))
            .portElasticSearch(getEnvOrDefault("LOCALSTACK_ELASTICSEARCH_PORT", properties.portElasticSearch()))
            .useSingleDockerContainer(properties.useSingleDockerContainer())
            .platform(StringUtils.isEmpty(properties.platform()) ? null : properties.platform())
            .build();
    }

    private String getEnvOrDefault(final String environmentVariable, final String defaultValue) {
        return System.getenv().getOrDefault(environmentVariable, defaultValue);
    }

    private Map<Integer, Integer> getCustomPortMappings(final LocalstackDockerProperties properties) {
        final Map<Integer, Integer> portMappings = new HashMap<>();
        for (String service : properties.services()) {
            String[] parts = service.split(":");
            if (parts.length > 1) {
                int port = Integer.parseInt(parts[1]);
                portMappings.put(port, port);
            }
        }
        return portMappings;
    }

    private Map<String, String> getEnvironments(final LocalstackDockerProperties properties) {
        final Map<String, String> environmentVariables = new HashMap<>();
        try {
            IEnvironmentVariableProvider environmentProvider = properties.environmentVariableProvider().newInstance();
            environmentVariables.putAll(environmentProvider.getEnvironmentVariables());
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to get environment variables", ex);
        }

        final String services = String.join(",", properties.services());
        if (StringUtils.isNotEmpty(services)) {
            environmentVariables.put("SERVICES", services);
        }
        return environmentVariables;
    }

    private Map<String, String> getBindMounts(final LocalstackDockerProperties properties) {
        try {
            IBindMountProvider environmentProvider = properties.bindMountProvider().newInstance();
            return new HashMap<>(environmentProvider.get());
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to get bind mounts", ex);
        }
    }

    private String getExternalHostName(final LocalstackDockerProperties properties) {
        try {
            IHostNameResolver hostNameResolver = properties.hostNameResolver().newInstance();
            String resolvedName = hostNameResolver.getHostName();

            final String externalHostName = StringUtils.defaultIfBlank(resolvedName, "localhost");

            LOG.fine("External host name is set to: " + externalHostName);
            return externalHostName;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to resolve hostname", ex);
        }
    }

}
