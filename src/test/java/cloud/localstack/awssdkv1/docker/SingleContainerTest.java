package cloud.localstack.docker;

import cloud.localstack.Localstack;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.Assert;
import org.junit.AfterClass;

@TestMethodOrder(OrderAnnotation.class)
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
        @Order(1)
        public void testCheckPort() {
            String endpoint = Localstack.INSTANCE.getEndpointSNS();
            checkAndSetEndpoint(endpoint);
        }
    }

    @ExtendWith(LocalstackDockerExtension.class)
    @LocalstackDockerProperties(randomizePorts=true, services={"sns"}, useSingleDockerContainer=true)
    public static class ContainerTest2 {

        @AfterClass
        @AfterAll
        public static void tearDown() {
            Localstack.INSTANCE.stop();
        }

        @org.junit.jupiter.api.Test
        @Order(2)
        public void testCheckPort() {
            String endpoint = Localstack.INSTANCE.getEndpointSNS();
            checkAndSetEndpoint(endpoint);
        }
    }
}
