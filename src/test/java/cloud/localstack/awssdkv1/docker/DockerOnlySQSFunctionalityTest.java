package cloud.localstack.docker;

import cloud.localstack.Localstack;
import cloud.localstack.CommonUtils;
import cloud.localstack.awssdkv1.TestUtils;
import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import io.thundra.jexter.junit4.core.envvar.EnvironmentVariableSandboxRule;
import io.thundra.jexter.junit5.core.envvar.EnvironmentVariableSandbox;
import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

@RunWith(LocalstackTestRunner.class)
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(randomizePorts = true, services = "sqs")
// [JUnit5] Revert environment variables to the back after the test suite (class)
@EnvironmentVariableSandbox
public class DockerOnlySQSFunctionalityTest {

    // [JUnit4] Revert environment variables to the back after the test suite (class)
    @ClassRule
    public static EnvironmentVariableSandboxRule environmentVariableSandboxRule = new EnvironmentVariableSandboxRule();

    @org.junit.BeforeClass
    @org.junit.jupiter.api.BeforeAll
    public static void beforeAll() {
        CommonUtils.setEnv("AWS_CBOR_DISABLE", "1");
    }

    @org.junit.Test
    @org.junit.jupiter.api.Test
    public void testKinesisNotRunning() {
        final Throwable throwable = Assertions.catchThrowable(() -> TestUtils.getClientKinesis().listStreams());

        Assertions.assertThat(throwable).isInstanceOf(SdkClientException.class);
    }

    // Should throw SdkClientException
    @org.junit.Test
    @org.junit.jupiter.api.Test
    public void testDynamoNotRunning() {
        final Throwable throwable = Assertions.catchThrowable(() -> TestUtils.getClientDynamoDB().listTables());

        Assertions.assertThat(throwable).isInstanceOf(SdkClientException.class);
    }

    @org.junit.Test
    @org.junit.jupiter.api.Test
    public void testS3NotRunning() {
        final Throwable throwable = Assertions.catchThrowable(() -> TestUtils.getClientS3().createBucket
                ("test-bucket"));

        Assertions.assertThat(throwable).isInstanceOf(SdkClientException.class);
    }

    @org.junit.Test
    @org.junit.jupiter.api.Test
    public void testSQSRunning() throws Exception {
        AmazonSQS client = TestUtils.getClientSQS();

        Map<String, String> attributeMap = new HashMap<>();
        attributeMap.put("DelaySeconds", "0");
        attributeMap.put("MaximumMessageSize", "262144");
        attributeMap.put("MessageRetentionPeriod", "1209600");
        attributeMap.put("ReceiveMessageWaitTimeSeconds", "20");
        attributeMap.put("VisibilityTimeout", "30");

        CreateQueueRequest createQueueRequest = new CreateQueueRequest("test-queue").withAttributes(attributeMap);
        client.createQueue(createQueueRequest);

        ListQueuesResult listQueuesResult = client.listQueues();

        Assertions.assertThat(listQueuesResult.getQueueUrls()).hasSize(1);

        SQSConnection connection = createSQSConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue queue = session.createQueue("test-queue");

        MessageProducer producer = session.createProducer(queue);
        TextMessage message = session.createTextMessage("Hello World!");
        producer.send(message);

        MessageConsumer consumer = session.createConsumer(queue);
        TextMessage received = (TextMessage) consumer.receive();

        Assertions.assertThat(received.getText()).isEqualTo("Hello World!");

        // clean up
        consumer.close();
        producer.close();
        connection.close();
    }

    private SQSConnection createSQSConnection() throws Exception {
        SQSConnectionFactory connectionFactory = SQSConnectionFactory.builder().withEndpoint(
                Localstack.INSTANCE.getEndpointSQS()).withAWSCredentialsProvider(
                new AWSStaticCredentialsProvider(TestUtils.TEST_CREDENTIALS)).build();
        return connectionFactory.createConnection();
    }

}
