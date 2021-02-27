package cloud.localstack.docker;

import cloud.localstack.Localstack;
import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(LocalstackTestRunner.class)
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(imageName = "localstack/localstack-full")
public class LocalstackDockerPropertiesWithImageNameTest {

    @Test
    @org.junit.jupiter.api.Test
    public void imageName() {
        String imageName = new DockerExe()
                .execute(Arrays.asList("container", "inspect",
                        Localstack.INSTANCE.getLocalStackContainer().getContainerId(),
                        "--format", "{{.Config.Image}}"));
        assertEquals("localstack/localstack-full", imageName);
    }
}
