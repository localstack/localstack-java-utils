package cloud.localstack.awssdkv2;

import cloud.localstack.Constants;
import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import cloud.localstack.sample.LambdaHandler;
import io.thundra.jexter.junit4.core.sysprop.SystemPropertySandboxRule;
import lombok.val;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.iam.IamAsyncClient;
import software.amazon.awssdk.services.iam.model.CreateUserRequest;
import software.amazon.awssdk.services.iam.model.User;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.CreateStreamResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;
import software.amazon.awssdk.services.lambda.model.Runtime;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(LocalstackTestRunner.class)
@LocalstackDockerProperties(ignoreDockerRunErrors=true)
public class BasicFeaturesSDKV2Test {

    // Revert system properties to the back after the test suite (class)
    @ClassRule
    public static SystemPropertySandboxRule systemPropertySandboxRule = new SystemPropertySandboxRule();

    @BeforeClass
    public static void beforeAll() {
        System.setProperty(SdkSystemSetting.CBOR_ENABLED.property(), "false");
    }

    @Test
    public void testCreateSqsQueueV2() throws Exception {
        String queueName = "test-q-"+ UUID.randomUUID().toString();
        CreateQueueRequest request = CreateQueueRequest.builder().queueName(queueName).build();
        SqsAsyncClient sqsClient = TestUtils.getClientSQSAsyncV2();
        CreateQueueResponse queue = sqsClient.createQueue(request).get();
        Assert.assertTrue(queue.queueUrl().contains(Constants.DEFAULT_AWS_ACCOUNT_ID + "/" + queueName));
    }

    @Test
    public void testCreateKinesisStreamV2() throws Exception {
        String streamName = "test-s-"+ UUID.randomUUID().toString();
        KinesisAsyncClient kinesisClient = TestUtils.getClientKinesisAsyncV2();
        CreateStreamRequest request = CreateStreamRequest.builder()
            .streamName(streamName).shardCount(1).build();
        CreateStreamResponse response = kinesisClient.createStream(request).get();
        Assert.assertNotNull(response);
    }

    @Test
    public void testCreateKinesisRecordV2() throws Exception {
        String streamName = "test-s-"+UUID.randomUUID().toString();
        KinesisAsyncClient kinesisClient = TestUtils.getClientKinesisAsyncV2();
        CreateStreamRequest request = CreateStreamRequest.builder()
            .streamName(streamName).shardCount(1).build();
        CreateStreamResponse response = kinesisClient.createStream(request).get();
        Assert.assertNotNull(response);

        SdkBytes payload = SdkBytes.fromByteBuffer(ByteBuffer.wrap(String.format("testData-%d", 1).getBytes()));
        PutRecordRequest.Builder putRecordRequest = PutRecordRequest.builder();
        putRecordRequest.streamName(streamName);
        putRecordRequest.data(payload);
        putRecordRequest.partitionKey(String.format("partitionKey-%d", 1));
        Assert.assertNotNull(kinesisClient.putRecord(putRecordRequest.build()));
    }

    @Test
    public void testCreateDynamoDBTable() throws Exception {
        DynamoDbAsyncClient dynamoDbAsyncClient = TestUtils.getClientDyanamoAsyncV2();
        String tableName = "test-s-"+ UUID.randomUUID().toString();
        CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .keySchema(
                        KeySchemaElement.builder()
                                .keyType(KeyType.HASH)
                                .attributeName("test")
                                .build()
                )
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("test")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .provisionedThroughput(
                        ProvisionedThroughput.builder()
                                .readCapacityUnits(5L)
                                .writeCapacityUnits(5L)
                                .build())
                .tableName(tableName)
                .build();
        CreateTableResponse response = dynamoDbAsyncClient.createTable(createTableRequest).get();
        Assert.assertNotNull(response);
        // clean up
        dynamoDbAsyncClient.deleteTable(DeleteTableRequest.builder().tableName(tableName).build());
    }

    @Test
    public void testS3CreateListBuckets() throws Exception {
        String bucketName = "test-b-"+UUID.randomUUID().toString();
        S3AsyncClient s3Client = TestUtils.getClientS3AsyncV2();
        CreateBucketRequest request = CreateBucketRequest.builder().bucket(bucketName).build();
        CreateBucketResponse response = s3Client.createBucket(request).get();
        Assert.assertNotNull(response);
        ListBucketsRequest listRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse buckets = s3Client.listBuckets(listRequest).get();
        Bucket bucket = buckets.buckets().stream().filter(b -> b.name().equals(bucketName)).findFirst().get();
        Assert.assertNotNull(bucket);
    }

    @Test
    public void testSendSNSMessage() throws Exception {
        // Test integration of SNS messaging with LocalStack using SDK v2

        final String topicName = "test-t-"+UUID.randomUUID().toString();
        final SnsAsyncClient clientSNS = TestUtils.getClientSNSAsyncV2();
        CreateTopicResponse createTopicResponse = clientSNS.createTopic(
            CreateTopicRequest.builder().name(topicName).build()).get();

        String topicArn = createTopicResponse.topicArn();
        Assert.assertNotNull(topicArn);
        PublishRequest publishRequest = PublishRequest.builder().topicArn(topicArn).subject("test subject").message("message test.").build();

        PublishResponse publishResponse = clientSNS.publish(publishRequest).get();
        Assert.assertNotNull(publishResponse.messageId());
    }

    @Test
    public void testGetSsmParameter() throws Exception {
        // Test integration of ssm parameter with LocalStack using SDK v2

        final SsmAsyncClient clientSsm = TestUtils.getClientSSMAsyncV2();
        final String paramName = "param-"+UUID.randomUUID().toString();
        clientSsm.putParameter(PutParameterRequest.builder().name(paramName).value("testvalue").build()).join();
        CompletableFuture<GetParameterResponse> getParameterResponse = clientSsm.getParameter(
            GetParameterRequest.builder().name(paramName).build());
        String parameterValue = getParameterResponse.get().parameter().value();
        Assert.assertNotNull(parameterValue);
        Assert.assertEquals("testvalue", parameterValue);
    }

    @Test
    public void testGetSecretsManagerSecret() throws Exception {
        final SecretsManagerAsyncClient clientSecretsManager = TestUtils.getClientSecretsManagerAsyncV2();
        final String secretName = "test-s-" + UUID.randomUUID().toString();
        clientSecretsManager.createSecret(
            CreateSecretRequest.builder().name(secretName).secretString("secretcontent").build()).join();
        CompletableFuture<GetSecretValueResponse> getSecretResponse = clientSecretsManager.getSecretValue(
            GetSecretValueRequest.builder().secretId(secretName).build());
        String secretValue = getSecretResponse.get().secretString();

        Assert.assertNotNull(secretValue);
        Assert.assertEquals("secretcontent", secretValue);

        // clean up
        clientSecretsManager.deleteSecret(DeleteSecretRequest.builder().secretId(secretName).build());
    }

    @Test
    public void testGetSecretAsParam() throws Exception {
        final SsmAsyncClient clientSsm = TestUtils.getClientSSMAsyncV2();
        final SecretsManagerAsyncClient clientSecretsManager = TestUtils.getClientSecretsManagerAsyncV2();

        final String secretName = "test-s-" + UUID.randomUUID().toString();
        clientSecretsManager.createSecret(CreateSecretRequest.builder()
            .name(secretName).secretString("secretcontent").build()).join();

        CompletableFuture<GetParameterResponse> getParameterResponse = clientSsm.getParameter(
            GetParameterRequest.builder().name("/aws/reference/secretsmanager/" + secretName).build());
        final String parameterValue = getParameterResponse.get().parameter().value();

        Assert.assertNotNull(parameterValue);
        Assert.assertEquals("secretcontent", parameterValue);

        // clean up
        clientSecretsManager.deleteSecret(DeleteSecretRequest.builder().secretId(secretName).build());
    }
    @Test
    public void testCWPutMetrics() throws Exception {
        final CloudWatchAsyncClient clientCW = TestUtils.getClientCloudWatchAsyncV2();
        Dimension dimension = Dimension.builder()
                    .name("UNIQUE_PAGES")
                    .value("URLS")
                    .build();

            // Set an Instant object
        String time = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );
        Instant instant = Instant.parse(time);

        double dataPoint = 1.23423;

        MetricDatum datum = MetricDatum.builder()
            .metricName("PAGES_VISITED")
            .unit(StandardUnit.NONE)
            .value(dataPoint)
            .timestamp(instant)
            .dimensions(dimension).build();

        PutMetricDataRequest request = PutMetricDataRequest.builder()
             .namespace("SITE/TRAFFIC")
             .metricData(datum).build();

        PutMetricDataResponse response = clientCW.putMetricData(request).get();
        Assert.assertNotNull(response);
    }
    
    @Test
    public void testCWMultipleDimentionsAndMetrics() throws Exception {
        final CloudWatchAsyncClient clientCW = TestUtils.getClientCloudWatchAsyncV2();
        
        List<Dimension> awsDimensionList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            awsDimensionList.add(Dimension.builder()
                    .name("UNIQUE_PAGES"+i)
                    .value("URLS"+i)
                    .build());
        }

        // Set an Instant object
        String time = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );
        Instant instant = Instant.parse(time);
        double dataPoint = 1.23423;
        
        List<MetricDatum> metrics = new ArrayList();
        for (int i = 0; i < 20; i++) {
            metrics.add(MetricDatum.builder()
            .metricName("PAGES_VISITED")
            .unit(StandardUnit.NONE)
            .value(dataPoint)
            .timestamp(instant)
            .dimensions(awsDimensionList).build());
        }

        PutMetricDataRequest request = PutMetricDataRequest.builder()
             .namespace("SITE/TRAFFIC")
             .metricData(metrics).build();

        PutMetricDataResponse response = clientCW.putMetricData(request).get();
        Assert.assertNotNull(response);
    }

    @Test
    public void testLambdaCreateListFunctions() throws Exception {
        val functionName = "test-f-"+UUID.randomUUID().toString();
        val lambdaClient = TestUtils.getClientLambdaAsyncV2();
        val createFunctionRequest = CreateFunctionRequest.builder().functionName(functionName)
                .runtime(Runtime.JAVA8)
                .role("r1")
                .code(LocalTestUtilSDKV2.createFunctionCode(LambdaHandler.class))
                .handler(LambdaHandler.class.getName()).build();
        val response = lambdaClient.createFunction(createFunctionRequest).get();
        Assert.assertNotNull(response);
        val functions = lambdaClient.listFunctions().get();
        val function = functions.functions().stream().filter(f -> f.functionName().equals(functionName)).findFirst().get();
        Assert.assertNotNull(function);
    }
	
    @Test
	public void testIAMUserCreation() throws Exception {
		IamAsyncClient iamClient = TestUtils.getClientIamAsyncV2();

		String username =  UUID.randomUUID().toString();
		CreateUserRequest createUserRequest = CreateUserRequest.builder().userName(username).build();
		iamClient.createUser(createUserRequest).join();
		
        boolean userFound = false;
        List<User> users = iamClient.listUsers().get().users();

        for (int i = 0; i < users.size(); i++) {
            if(users.get(i).userName().equals(username)){
                userFound = true;
                break;
            }
        }

		Assert.assertTrue(userFound);
	}
	
    @Test
    public void testIAMListUserPagination() throws Exception {
		IamAsyncClient iamClient = TestUtils.getClientIamAsyncV2();

		String username = UUID.randomUUID().toString();
		CreateUserRequest createUserRequest = CreateUserRequest.builder().userName(username).build();
		iamClient.createUser(createUserRequest).join();

        AtomicBoolean userFound = new AtomicBoolean(false);
        iamClient.listUsersPaginator().users().subscribe(user -> {
            if(user.userName().equals(username)){
                userFound.set(true);
            }
        });

        TimeUnit.SECONDS.sleep(2);
        Assert.assertTrue(userFound.get());
	}

}
