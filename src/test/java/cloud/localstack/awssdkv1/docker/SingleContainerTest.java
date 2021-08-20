package cloud.localstack.docker;

import cloud.localstack.Localstack;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(randomizePorts=true, services={"sns"}, useSingleDockerContainer=true)
@TestMethodOrder(OrderAnnotation.class)
public class SingleContainerTest {

    static String SNS_ENDPOINT = "";

    static void checkAndSetEndpoint(String endpoint) {
        if (!SNS_ENDPOINT.equals("")) {
            Assert.assertEquals(SNS_ENDPOINT, endpoint);
        }
        SNS_ENDPOINT = endpoint;
    }

    @AfterClass
    @AfterAll
    public static void tearDown() {
        Localstack.INSTANCE.stop();
    }

    @org.junit.jupiter.api.Test
    @Order(1)
    public void testCheckPort() {
        String endpoint = Localstack.INSTANCE.getEndpointSNS();
        checkAndSetEndpoint(endpoint);
    }

    @org.junit.jupiter.api.Test
    @Order(2)
    public void testCheckPort2() {
        String endpoint = Localstack.INSTANCE.getEndpointSNS();
        checkAndSetEndpoint(endpoint);
    }

}
