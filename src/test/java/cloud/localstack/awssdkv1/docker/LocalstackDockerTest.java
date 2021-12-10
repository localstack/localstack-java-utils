package cloud.localstack.docker;

import cloud.localstack.Localstack;
import cloud.localstack.awssdkv1.TestUtils;
import cloud.localstack.docker.annotation.LocalstackDockerConfiguration;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;

public class LocalstackDockerTest {

    private static final LocalstackDockerConfiguration DOCKER_CONFIG = LocalstackDockerConfiguration.builder()
            .randomizePorts(true)
            .build();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void startup() {
        Localstack localstackDocker = Localstack.INSTANCE;
        Localstack.INSTANCE.startup(DOCKER_CONFIG);

        AmazonSQS amazonSQS = TestUtils.getClientSQS();
        String queueUrl = amazonSQS.createQueue("test-queue").getQueueUrl();

        SendMessageResult sendMessageResult = amazonSQS.sendMessage(queueUrl, "test-message");
        assertNotNull(sendMessageResult);

        String messageId = sendMessageResult.getMessageId();
        assertNotNull(messageId);

        thrown.expect(IllegalStateException.class);

        Localstack.INSTANCE.startup(DOCKER_CONFIG);
        Localstack.INSTANCE.stop();
    }

    @Test
    public void stop() {
        Localstack.INSTANCE.startup(DOCKER_CONFIG);
        Localstack.INSTANCE.stop();

        AmazonSQS amazonSQS = TestUtils.getClientSQS();
        thrown.expect(SdkClientException.class);
        amazonSQS.createQueue("test-queue").getQueueUrl();
    }

    @Test
    public void restart() {
        Localstack.INSTANCE.startup(DOCKER_CONFIG);
        Localstack.INSTANCE.stop();
        assertFalse(Localstack.INSTANCE.isRunning());
    }

    @After
    public void tearDown() {
        Localstack.INSTANCE.stop();
    }
}
