package cloud.localstack.docker;

import cloud.localstack.Localstack;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ContainerTest {

    public static final String EXTERNAL_HOST_NAME = "localhost";
    public static final String MY_PROPERTY = "MY_PROPERTY";
    public static final String MY_VALUE = "MyValue";

    public static final boolean pullNewImage = false;

    @Test
    public void createLocalstackContainer() throws Exception {

        HashMap<String, String> environmentVariables = new HashMap<>();
        environmentVariables.put(MY_PROPERTY, MY_VALUE);
        Container localStackContainer = Container.createLocalstackContainer(
                EXTERNAL_HOST_NAME, pullNewImage, false, null, null, null, null, environmentVariables, null);

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

    @Test
    public void createLocalstackContainerWithFullImage() {

        String customImageName = "localstack/localstack-full";
        Container localStackContainer = Container.createLocalstackContainer(
                EXTERNAL_HOST_NAME, pullNewImage, false, customImageName, null, null, null, null, null);

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

    @ExtendWith(LocalstackDockerExtension.class)
    @LocalstackDockerProperties(imageName = "localstack/localstack-full")
    public static class ContainerTest1 {
        @org.junit.jupiter.api.Test
        public void imageName() {
            String imageName = new DockerExe()
                    .execute(Arrays.asList("container", "inspect",
                            Localstack.INSTANCE.getLocalStackContainer().getContainerId(),
                            "--format", "{{.Config.Image}}"));
            assertEquals("localstack/localstack-full", imageName);
        }
    }

    @Test
    public void createLocalstackContainerWithCustomPorts() throws Exception {
        Container localStackContainer = Container.createLocalstackContainer(
                EXTERNAL_HOST_NAME, pullNewImage, false, null, null, "45660", "45710", null, null);

        try {
            localStackContainer.waitForAllPorts(EXTERNAL_HOST_NAME);

            assertEquals(45660, localStackContainer.getExternalPortFor(4566));
            assertEquals(45710, localStackContainer.getExternalPortFor(4571));
        }
        finally {
            localStackContainer.stop();
        }
    }

    @Test
    public void createLocalstackContainerWithRandomPorts() throws Exception {
        Container localStackContainer = Container.createLocalstackContainer(
                EXTERNAL_HOST_NAME, pullNewImage, false, null, null, ":4566", ":4571", null, null);

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
