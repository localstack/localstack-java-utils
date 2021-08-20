package cloud.localstack.docker;

import cloud.localstack.CommonUtils;
import cloud.localstack.Localstack;
import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.awssdkv1.TestUtils;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricDataResult;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.CreateStreamRequest;
import com.amazonaws.services.kinesis.model.ListStreamsResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.util.IOUtils;
import io.thundra.jexter.junit4.core.envvar.EnvironmentVariableSandboxRule;
import io.thundra.jexter.junit4.core.sysprop.SystemPropertySandboxRule;
import io.thundra.jexter.junit5.core.envvar.EnvironmentVariableSandbox;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(LocalstackTestRunner.class)
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(randomizePorts = true)
// [JUnit5] Revert environment variables to the back after the test suite (class)
@EnvironmentVariableSandbox
public class BasicDockerFunctionalityTest {

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
    public void testSecretsManager() throws Exception {
        AWSSecretsManager secretsManager = TestUtils.getClientSecretsManager();

        CreateSecretRequest createSecretRequest = new CreateSecretRequest();
        createSecretRequest.setName("my-secret-name");
        createSecretRequest.setSecretString("this is a secret thing");
        secretsManager.createSecret(createSecretRequest);

        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId("my-secret-name");
        String result = secretsManager.getSecretValue(getSecretValueRequest).getSecretString();
        Assertions.assertThat(result).isEqualTo("this is a secret thing");

    }

    @org.junit.Test
    @org.junit.jupiter.api.Test
    public void testKinesis() throws Exception {
        AmazonKinesis kinesis = TestUtils.getClientKinesis();

        ListStreamsResult streamsResult = kinesis.listStreams();

        Assertions.assertThat(streamsResult.getStreamNames()).isEmpty();

        CreateStreamRequest createStreamRequest = new CreateStreamRequest()
                .withStreamName("test-stream")
                .withShardCount(2);

        kinesis.createStream(createStreamRequest);

        streamsResult = kinesis.listStreams();
        Assertions.assertThat(streamsResult.getStreamNames()).contains("test-stream");
    }

    @org.junit.Test
    @org.junit.jupiter.api.Test
    public void testDynamo() throws Exception {
        AmazonDynamoDB dynamoDB = TestUtils.getClientDynamoDB();

        ListTablesResult tablesResult = dynamoDB.listTables();
        Assertions.assertThat(tablesResult.getTableNames()).hasSize(0);

        CreateTableRequest createTableRequest = new CreateTableRequest()
                .withTableName("test.table")
                .withKeySchema(new KeySchemaElement("identifier", KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition("identifier", ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput(10L, 10L));
        dynamoDB.createTable(createTableRequest);

        tablesResult = dynamoDB.listTables();
        Assertions.assertThat(tablesResult.getTableNames()).contains("test.table");
    }

    @org.junit.Test
    @org.junit.jupiter.api.Test
    public void testS3() throws Exception {
        AmazonS3 client = TestUtils.getClientS3();

        client.createBucket("test-bucket");
        List<Bucket> bucketList = client.listBuckets();

        Assertions.assertThat(bucketList).hasSize(1);

        File file = File.createTempFile("localstack", "s3");
        file.deleteOnExit();

        try (FileOutputStream stream = new FileOutputStream(file)) {
            String content = "HELLO WORLD!";
            stream.write(content.getBytes());
        }

        PutObjectRequest request = new PutObjectRequest("test-bucket", "testData", file);
        client.putObject(request);

        ObjectListing listing = client.listObjects("test-bucket");
        Assertions.assertThat(listing.getObjectSummaries()).hasSize(1);

        S3Object s3Object = client.getObject("test-bucket", "testData");
        String resultContent = IOUtils.toString(s3Object.getObjectContent());

        Assertions.assertThat(resultContent).isEqualTo("HELLO WORLD!");
    }

    @org.junit.Test
    @org.junit.jupiter.api.Test
    public void testSQS() throws Exception {
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

    @org.junit.Test
    @org.junit.jupiter.api.Test
    public void testCloudWatch() throws Exception {
        AmazonCloudWatch client = TestUtils.getClientCloudWatch();
        Dimension dimension = new Dimension()
            .withName("UNIQUE_PAGES")
            .withValue("URLS");
        MetricDatum datum = new MetricDatum()
            .withMetricName("PAGES_VISITED")
            .withUnit(StandardUnit.None)
            .withDimensions(dimension);
        PutMetricDataRequest request = new PutMetricDataRequest()
            .withNamespace("SITE/TRAFFIC")
            .withMetricData(datum);
        // assert no error gets thrown for null values
        datum.setValue(null);
        PutMetricDataResult response = client.putMetricData(request);
        Assert.assertNotNull(response);
        // assert success for double values
        datum.setValue(123.4);
        response = client.putMetricData(request);
        Assert.assertNotNull(response);
    }

    @org.junit.Test
    @org.junit.jupiter.api.Test
    public void testCreateDuplicateRoleException() {
        AmazonIdentityManagement iamClient = TestUtils.getClientIAM();
        try {
            CreateRoleRequest request = new CreateRoleRequest().withRoleName("role1234");
            iamClient.createRole(request);
            iamClient.createRole(request);
            throw new RuntimeException("EntityAlreadyExistsException should be thrown");
        } catch (EntityAlreadyExistsException e) {
            // this exception is expected here
        }
    }

    private SQSConnection createSQSConnection() throws Exception {
        SQSConnectionFactory connectionFactory = SQSConnectionFactory.builder().withEndpoint(
                Localstack.INSTANCE.getEndpointSQS()).withAWSCredentialsProvider(
                new AWSStaticCredentialsProvider(TestUtils.TEST_CREDENTIALS)).build();
        return connectionFactory.createConnection();
    }

}
