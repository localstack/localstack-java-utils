package cloud.localstack.docker;

import cloud.localstack.Localstack;
import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.docker.annotation.IBindMountProvider;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(LocalstackTestRunner.class)
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(bindMountProvider = LocalstackDockerPropertiesWithBindMountTest.TestMounts.class,
    initializationToken = "testmarker")
public class LocalstackDockerPropertiesWithBindMountTest {

    @Test
    @org.junit.jupiter.api.Test
    public void bindMound() {
        assertEquals("echo testmarker", Localstack.INSTANCE.getLocalStackContainer()
                .executeCommand(Arrays.asList("cat", Localstack.INIT_SCRIPTS_PATH + "/02-init-script-test.sh")));
    }

    public static class TestMounts extends IBindMountProvider.BaseBindMountProvider {

        @Override
        protected void initValues(Map<String, String> mounts) {
            mounts.put(new File("./src/test/resources/01-init-script-test.sh").getAbsolutePath(), Localstack.INIT_SCRIPTS_PATH + "/01-init-script-test.sh");
            mounts.put(ContainerTest.testFile("echo testmarker"), Localstack.INIT_SCRIPTS_PATH + "/02-init-script-test.sh");
        }
    }
}
