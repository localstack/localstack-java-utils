package cloud.localstack.docker.annotation;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Bean to specify the docker configuration.
 *
 * @author Patrick Allain
 * @author Waldemar Hummer
 * @author Omar Khammassi
 */
@Data
@Builder
public class LocalstackDockerConfiguration {

    public static final LocalstackDockerConfiguration DEFAULT = LocalstackDockerConfiguration.builder().build();

    private final boolean pullNewImage;

    private final boolean randomizePorts;

    private final String imageName;
    private final String imageTag;
    private final String platform;

    @Builder.Default
    private final String portEdge = "4566";

    @Builder.Default
    private final String portElasticSearch = "4571";

    @Builder.Default
    private final String externalHostName = "localhost";

    @Builder.Default
    private final Map<String, String> environmentVariables = Collections.emptyMap();

    @Builder.Default
    private final Map<Integer, Integer> portMappings = Collections.emptyMap();

    @Builder.Default
    private final Map<String, String> bindMounts = Collections.emptyMap();

    private final Pattern initializationToken;

    @Builder.Default
    private final boolean useSingleDockerContainer = false;

    @Builder.Default
    private final boolean ignoreDockerRunErrors = false;

}
