package cloud.localstack.docker;

import cloud.localstack.Localstack;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ContainerTest {

    public static final String EXTERNAL_HOST_NAME = "localhost";
    public static final String MY_PROPERTY = "MY_PROPERTY";
    public static final String MY_VALUE = "MyValue";

    public static final boolean pullNewImage = false;

    @org.junit.jupiter.api.Test
    @Test
    public void createLocalstackContainer() throws Exception {

        HashMap<String, String> environmentVariables = new HashMap<>();
        environmentVariables.put(MY_PROPERTY, MY_VALUE);
        Container localStackContainer = Container.createLocalstackContainer(
                EXTERNAL_HOST_NAME, pullNewImage, false, null, null, null, null, environmentVariables, null, null, null);

        try {
            localStackContainer.waitForAllPorts(EXTERNAL_HOST_NAME);

            // Test that environment variables are actually loaded

            ArrayList<String> echoDefaultEnv = buildEchoStatement(Container.LOCALSTACK_EXTERNAL_HOSTNAME);
            ArrayList<String> echoExternalEnv = buildEchoStatement(MY_PROPERTY);
            assertEquals(EXTERNAL_HOST_NAME, localStackContainer.executeCommand(echoDefaultEnv));
            assertEquals(MY_VALUE, localStackContainer.executeCommand(echoExternalEnv));

            // Test Edge and ElasticSearch ports

            assertEquals(4566, localStackContainer.getExternalPortFor(4566));
            assertEquals(4571, localStackContainer.getExternalPortFor(4571));
        }
        finally {
            localStackContainer.stop();
        }
    }

    @org.junit.jupiter.api.Test
    @Test
    public void createLocalstackContainerWithFullImage() {

        String customImageName = "localstack/localstack-full";
        Container localStackContainer = Container.createLocalstackContainer(
                EXTERNAL_HOST_NAME, pullNewImage, false, customImageName, null, null, null, null, null, null, null);

        try {
            localStackContainer.waitForAllPorts(EXTERNAL_HOST_NAME);

            String imageName = new DockerExe()
                    .execute(Arrays.asList("container", "inspect",
                            localStackContainer.getContainerId(), "--format", "{{.Config.Image}}"));
            assertEquals(customImageName, imageName);
        }
        finally {
            localStackContainer.stop();
        }
    }

    @org.junit.jupiter.api.Test
    @Test
    public void createLocalstackContainerWithScriptMounted() {

        Container localStackContainer = Container.createLocalstackContainer(
                EXTERNAL_HOST_NAME, pullNewImage, false, null, null, null, null, null, null,
                Collections.singletonMap(testFile("echo testmarker"), Localstack.INIT_SCRIPTS_PATH + "/test.sh"), null);

        try {
            localStackContainer.waitForAllPorts(EXTERNAL_HOST_NAME);
            localStackContainer.waitForLogToken(Pattern.compile("testmarker"));
            assertEquals("echo testmarker", localStackContainer.executeCommand(Arrays.asList("cat", Localstack.INIT_SCRIPTS_PATH + "/test.sh")));

        }
        finally {
            localStackContainer.stop();
        }
    }

    static String testFile(String content) {
        try {
            File testFile = File.createTempFile("localstack", ".sh");
            FileUtils.writeStringToFile(testFile, content, StandardCharsets.UTF_8);
            testFile.deleteOnExit();
            return testFile.getAbsolutePath();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @org.junit.jupiter.api.Test
    @Test
    public void createLocalstackContainerWithCustomPorts() throws Exception {
        Container localStackContainer = Container.createLocalstackContainer(
                EXTERNAL_HOST_NAME, pullNewImage, false, null, null, "45660", "45710", null, null, null, null);

        try {
            localStackContainer.waitForAllPorts(EXTERNAL_HOST_NAME);

            assertEquals(45660, localStackContainer.getExternalPortFor(4566));
            assertEquals(45710, localStackContainer.getExternalPortFor(4571));
        }
        finally {
            localStackContainer.stop();
        }
    }

    @org.junit.jupiter.api.Test
    @Test
    public void createLocalstackContainerWithRandomPorts() throws Exception {
        Container localStackContainer = Container.createLocalstackContainer(
                EXTERNAL_HOST_NAME, pullNewImage, false, null, null, ":4566", ":4571", null, null, null, null);

        try {
            localStackContainer.waitForAllPorts(EXTERNAL_HOST_NAME);

            assertNotEquals(4566, localStackContainer.getExternalPortFor(4566));
            assertNotEquals(4571, localStackContainer.getExternalPortFor(4571));
        }
        finally {
            localStackContainer.stop();
        }
    }

    private ArrayList<String> buildEchoStatement(String valueToEcho) {
        ArrayList<String> args = new ArrayList<>();
        args.add("bash");
        args.add("-c");
        args.add(String.format("echo $%s", valueToEcho));
        return args;
    }

}
