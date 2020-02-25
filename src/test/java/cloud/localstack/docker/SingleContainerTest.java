package cloud.localstack.docker;

import cloud.localstack.Localstack;
import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.TestUtils;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.Assert;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;

public class SingleContainerTest {

    static String SNS_ENDPOINT = "";

    static void checkAndSetEndpoint(String endpoint) {
        if (!SNS_ENDPOINT.equals("")) {
            Assert.assertEquals(SNS_ENDPOINT, endpoint);
        }
        SNS_ENDPOINT = endpoint;
    }

    @ExtendWith(LocalstackDockerExtension.class)
    @LocalstackDockerProperties(randomizePorts=true, services={"sns"}, useSingleDockerContainer=true)
    public static class ContainerTest1 {
        @org.junit.jupiter.api.Test
        public void testCheckPort() {
            String endpoint = Localstack.INSTANCE.getEndpointSNS();
            checkAndSetEndpoint(endpoint);
        }
    }

    @ExtendWith(LocalstackDockerExtension.class)
    @LocalstackDockerProperties(randomizePorts=true, services={"sns"}, useSingleDockerContainer=true)
    public static class ContainerTest2 {
        @org.junit.jupiter.api.Test
        public void testCheckPort() {
            String endpoint = Localstack.INSTANCE.getEndpointSNS();
            checkAndSetEndpoint(endpoint);
        }
    }
}
