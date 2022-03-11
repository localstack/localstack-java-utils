package cloud.localstack.awssdkv2;

import cloud.localstack.Constants;
import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.awssdkv1.sample.SQSLambdaHandler;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import cloud.localstack.sample.LambdaHandler;
import cloud.localstack.utils.LocalTestUtil;
import com.amazonaws.services.s3.model.ObjectListing;
import io.thundra.jexter.junit4.core.sysprop.SystemPropertySandboxRule;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.utility.ThrowingFunction;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.iam.IamAsyncClient;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreateUserRequest;
import software.amazon.awssdk.services.iam.model.CreateUserResponse;
import software.amazon.awssdk.services.iam.model.ListUsersResponse;
import software.amazon.awssdk.services.iam.model.User;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.*;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;
import software.amazon.awssdk.services.lambda.model.CreateFunctionResponse;
import software.amazon.awssdk.services.lambda.model.ListFunctionsResponse;
import software.amazon.awssdk.services.lambda.model.Runtime;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;

import static org.junit.Assert.assertThrows;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
    public void testCreateSqsQueueAsyncV2() throws Exception {
        SqsAsyncClient sqsAsyncClient = TestUtils.getClientSQSAsyncV2();
        validateCreateSqsQueueV2(createReq -> sqsAsyncClient.createQueue(createReq).get());
    }

    @Test
    public void testCreateSqsQueueV2() throws Exception {
        SqsClient sqsClient = TestUtils.getClientSQSV2();
        validateCreateSqsQueueV2(createReq -> sqsClient.createQueue(createReq));
    }

    protected static void validateCreateSqsQueueV2(
        ThrowingFunction<CreateQueueRequest, CreateQueueResponse> createAction
    ) throws Exception {
        String queueName = "test-q-"+ UUID.randomUUID().toString();
        CreateQueueRequest request = CreateQueueRequest.builder().queueName(queueName).build();
        CreateQueueResponse queue = createAction.apply(request);
        Assert.assertTrue(queue.queueUrl().contains(Constants.DEFAULT_AWS_ACCOUNT_ID + "/" + queueName));
    }

    @Test
    public void testCreateKinesisStreamAsyncV2() throws Exception {
        KinesisAsyncClient kinesisAsyncClient = TestUtils.getClientKinesisAsyncV2();
        validateCreateKinesisStreamV2(createReq -> kinesisAsyncClient.createStream(createReq).get());
    }

    @Test
    public void testCreateKinesisStreamV2() throws Exception {
        KinesisClient kinesisClient = TestUtils.getClientKinesisV2();
        validateCreateKinesisStreamV2(createReq -> kinesisClient.createStream(createReq));
    }

    protected static void validateCreateKinesisStreamV2(
        ThrowingFunction<CreateStreamRequest, CreateStreamResponse> createAction
    ) throws Exception {
        String streamName = "test-s-"+ UUID.randomUUID().toString();
        CreateStreamRequest request = CreateStreamRequest.builder()
            .streamName(streamName).shardCount(1).build();
        CreateStreamResponse response = createAction.apply(request);
        Assert.assertNotNull(response);
    }

    @Test
    public void testCreateKinesisRecordAsyncV2() throws Exception {
        KinesisAsyncClient kinesisAsyncClient = TestUtils.getClientKinesisAsyncV2();
        validateCreateKinesisRecordV2(
            createReq -> kinesisAsyncClient.createStream(createReq).get(),
            describeReq -> kinesisAsyncClient.describeStream(describeReq).get(),
            putReq -> kinesisAsyncClient.putRecord(putReq).get()
        );
    }

    @Test
    public void testCreateKinesisRecordV2() throws Exception {
        KinesisClient kinesisClient = TestUtils.getClientKinesisV2();
        validateCreateKinesisRecordV2(
            createReq -> kinesisClient.createStream(createReq),
            describeReq -> kinesisClient.describeStream(describeReq),
            putReq -> kinesisClient.putRecord(putReq)
        );
    }

    protected static void validateCreateKinesisRecordV2(
        ThrowingFunction<CreateStreamRequest, CreateStreamResponse> createAction,
        ThrowingFunction<DescribeStreamRequest, DescribeStreamResponse> describeAction,
        ThrowingFunction<PutRecordRequest, PutRecordResponse> putAction
    ) throws Exception {
        String streamName = "test-s-"+UUID.randomUUID().toString();
        CreateStreamRequest request = CreateStreamRequest.builder()
            .streamName(streamName).shardCount(1).build();
        CreateStreamResponse response = createAction.apply(request);
        Assert.assertNotNull(response);

        // wait for the stream to become active
        DescribeStreamRequest describeStreamRequest = DescribeStreamRequest.builder().streamName(streamName).build();
        Runnable check = new Runnable() {
            @SneakyThrows
            public void run() {
                DescribeStreamResponse describeResponse = describeAction.apply(describeStreamRequest);
                Assert.assertNotNull(describeResponse);
                Assert.assertEquals(describeResponse.streamDescription().streamStatus(), StreamStatus.ACTIVE);
            }
        };
        LocalTestUtil.retry(check, 5, 1);

        SdkBytes payload = SdkBytes.fromByteBuffer(ByteBuffer.wrap(String.format("testData-%d", 1).getBytes()));
        PutRecordRequest.Builder putRecordRequest = PutRecordRequest.builder();
        putRecordRequest.streamName(streamName);
        putRecordRequest.data(payload);
        putRecordRequest.partitionKey(String.format("partitionKey-%d", 1));
        Assert.assertNotNull(putAction.apply(putRecordRequest.build()));
    }

    @Test
    public void testCreateDynamoDBTableAsync() throws Exception {
        DynamoDbAsyncClient dynamoDbAsyncClient = TestUtils.getClientDyanamoAsyncV2();
        validateCreateDynamoDBTable(
            createReq -> dynamoDbAsyncClient.createTable(createReq).get(),
            deleteReq -> dynamoDbAsyncClient.deleteTable(deleteReq).get()
        );
    }

    @Test
    public void testCreateDynamoDBTable() throws Exception {
        DynamoDbClient dynamoDbClient = TestUtils.getClientDyanamoV2();
        validateCreateDynamoDBTable(
            createReq -> dynamoDbClient.createTable(createReq),
            deleteReq -> dynamoDbClient.deleteTable(deleteReq)
        );
    }

    protected static void validateCreateDynamoDBTable(
        ThrowingFunction<CreateTableRequest, CreateTableResponse> createAction,
        ThrowingFunction<DeleteTableRequest, DeleteTableResponse> deleteAction
    ) throws Exception {
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
        CreateTableResponse response = createAction.apply(createTableRequest);
        Assert.assertNotNull(response);
        // clean up
        deleteAction.apply(DeleteTableRequest.builder().tableName(tableName).build());
    }

    @Test
    public void testS3CreateListBucketsAsync() throws Exception {
        S3AsyncClient s3AsyncClient = TestUtils.getClientS3AsyncV2();
        validateS3CreateListBuckets(
            createReq -> s3AsyncClient.createBucket(createReq).get(),
            listReq -> s3AsyncClient.listBuckets(listReq).get()
        );
    }

    @Test
    public void testS3CreateListBuckets() throws Exception {
        S3Client s3Client = TestUtils.getClientS3V2();
        validateS3CreateListBuckets(
            createReq -> s3Client.createBucket(createReq),
            listReq -> s3Client.listBuckets(listReq)
        );
    }

    protected static void validateS3CreateListBuckets(
        ThrowingFunction<CreateBucketRequest, CreateBucketResponse> createAction,
        ThrowingFunction<ListBucketsRequest, ListBucketsResponse> listAction
    ) throws Exception {
        String bucketName = "test-b-"+UUID.randomUUID().toString();
        CreateBucketRequest request = CreateBucketRequest.builder().bucket(bucketName).build();
        CreateBucketResponse response = createAction.apply(request);
        Assert.assertNotNull(response);
        ListBucketsRequest listRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse buckets = listAction.apply(listRequest);
        Bucket bucket = buckets.buckets().stream().filter(b -> b.name().equals(bucketName)).findFirst().get();
        Assert.assertNotNull(bucket);
    }

    @Test
    public void testSendSNSMessageAsync() throws Exception {
        final SnsAsyncClient snsAsyncClient = TestUtils.getClientSNSAsyncV2();
        validateSendSNSMessage(
            createReq -> snsAsyncClient.createTopic(createReq).get(),
            publishReq -> snsAsyncClient.publish(publishReq).get()
        );
    }

    @Test
    public void testSendSNSMessage() throws Exception {
        final SnsClient snsClient = TestUtils.getClientSNSV2();
        validateSendSNSMessage(
            createReq -> snsClient.createTopic(createReq),
            publishReq -> snsClient.publish(publishReq)
        );
    }

    protected void validateSendSNSMessage(
        ThrowingFunction<CreateTopicRequest, CreateTopicResponse> createAction,
        ThrowingFunction<PublishRequest, PublishResponse> publishAction
    ) throws Exception {
        // Test integration of SNS messaging with LocalStack using SDK v2

        final String topicName = "test-t-"+UUID.randomUUID().toString();
        CreateTopicResponse createTopicResponse = createAction.apply(
            CreateTopicRequest.builder().name(topicName).build());

        String topicArn = createTopicResponse.topicArn();
        Assert.assertNotNull(topicArn);
        PublishRequest publishRequest = PublishRequest.builder().topicArn(topicArn).subject("test subject").message("message test.").build();

        PublishResponse publishResponse = publishAction.apply(publishRequest);
        Assert.assertNotNull(publishResponse.messageId());
    }

    @Test
    public void testGetSsmParameterAsync() throws Exception {
        final SsmAsyncClient ssmAsyncClient = TestUtils.getClientSSMAsyncV2();
        validateGetSsmParameter(
            putReq -> ssmAsyncClient.putParameter(putReq).get(),
            getReq -> ssmAsyncClient.getParameter(getReq).get()
        );
    }

    @Test
    public void testGetSsmParameter() throws Exception {
        final SsmClient ssmClient = TestUtils.getClientSSMV2();
        validateGetSsmParameter(
            putReq -> ssmClient.putParameter(putReq),
            getReq -> ssmClient.getParameter(getReq)
        );
    }

    protected static void validateGetSsmParameter(
        ThrowingFunction<PutParameterRequest, PutParameterResponse> putAction,
        ThrowingFunction<GetParameterRequest, GetParameterResponse> getAction
    ) throws Exception {
        // Test integration of ssm parameter with LocalStack using SDK v2

        final String paramName = "param-"+UUID.randomUUID().toString();
        putAction.apply(PutParameterRequest.builder().name(paramName).value("testvalue").build());
        GetParameterResponse getParameterResponse = getAction.apply(
            GetParameterRequest.builder().name(paramName).build());
        String parameterValue = getParameterResponse.parameter().value();
        Assert.assertNotNull(parameterValue);
        Assert.assertEquals("testvalue", parameterValue);
    }

    @Test
    public void testGetSecretsManagerSecretAsync() throws Exception {
        final SecretsManagerAsyncClient secretsManagerAsync = TestUtils.getClientSecretsManagerAsyncV2();
        validateGetSecretsManagerSecret(
            createReq -> secretsManagerAsync.createSecret(createReq).get(),
            getReq -> secretsManagerAsync.getSecretValue(getReq).get(),
            delReq -> secretsManagerAsync.deleteSecret(delReq).get()
        );
    }

    @Test
    public void testGetSecretsManagerSecret() throws Exception {
        final SecretsManagerClient secretsManager = TestUtils.getClientSecretsManagerV2();
        validateGetSecretsManagerSecret(
            createReq -> secretsManager.createSecret(createReq),
            getReq -> secretsManager.getSecretValue(getReq),
            delReq -> secretsManager.deleteSecret(delReq)
        );
    }

    protected static void validateGetSecretsManagerSecret(
        ThrowingFunction<CreateSecretRequest, CreateSecretResponse> createAction,
        ThrowingFunction<GetSecretValueRequest, GetSecretValueResponse> getAction,
        ThrowingFunction<DeleteSecretRequest, DeleteSecretResponse> deleteAction
    ) throws Exception {
        final String secretName = "test-s-" + UUID.randomUUID().toString();
        createAction.apply(
            CreateSecretRequest.builder().name(secretName).secretString("secretcontent").build());
        GetSecretValueResponse getSecretResponse = getAction.apply(
            GetSecretValueRequest.builder().secretId(secretName).build());
        String secretValue = getSecretResponse.secretString();

        Assert.assertNotNull(secretValue);
        Assert.assertEquals("secretcontent", secretValue);

        // clean up
        deleteAction.apply(DeleteSecretRequest.builder().secretId(secretName).build());
    }

    @Test
    public void testGetSecretAsParamAsync() throws Exception {
        final SsmAsyncClient ssmAsyncClient = TestUtils.getClientSSMAsyncV2();
        final SecretsManagerAsyncClient secretsManagerAsyncClient = TestUtils.getClientSecretsManagerAsyncV2();

        validateGetSecretAsParam(
            createReq -> secretsManagerAsyncClient.createSecret(createReq).get(),
            getReq -> ssmAsyncClient.getParameter(getReq).get(),
            delReq -> secretsManagerAsyncClient.deleteSecret(delReq).get()
        );
    }

    @Test
    public void testGetSecretAsParam() throws Exception {
        final SsmClient ssmClient = TestUtils.getClientSSMV2();
        final SecretsManagerClient secretsManagerClient = TestUtils.getClientSecretsManagerV2();

        validateGetSecretAsParam(
            createReq -> secretsManagerClient.createSecret(createReq),
            getReq -> ssmClient.getParameter(getReq),
            delReq -> secretsManagerClient.deleteSecret(delReq)
        );
    }

    protected static void validateGetSecretAsParam(
        ThrowingFunction<CreateSecretRequest, CreateSecretResponse> createAction,
        ThrowingFunction<GetParameterRequest, GetParameterResponse> getAction,
        ThrowingFunction<DeleteSecretRequest, DeleteSecretResponse> delAction
    ) throws Exception {
        final String secretName = "test-s-" + UUID.randomUUID().toString();
        createAction.apply(CreateSecretRequest.builder()
            .name(secretName).secretString("secretcontent").build());

        GetParameterResponse getParameterResponse = getAction.apply(
            GetParameterRequest.builder().name("/aws/reference/secretsmanager/" + secretName).build());
        final String parameterValue = getParameterResponse.parameter().value();

        Assert.assertNotNull(parameterValue);
        Assert.assertEquals("secretcontent", parameterValue);

        // clean up
        delAction.apply(DeleteSecretRequest.builder().secretId(secretName).build());
    }

    @Test
    public void testCWPutMetricsAsync() throws Exception {
        final CloudWatchAsyncClient cwClientAsync = TestUtils.getClientCloudWatchAsyncV2();
        validateCWPutMetrics(
            putReq -> cwClientAsync.putMetricData(putReq).get()
        );
    }

    @Test
    public void testCWPutMetrics() throws Exception {
        final CloudWatchClient cwClient = TestUtils.getClientCloudWatchV2();
        validateCWPutMetrics(
            putReq -> cwClient.putMetricData(putReq)
        );
    }

    protected static void validateCWPutMetrics(
        ThrowingFunction<PutMetricDataRequest, PutMetricDataResponse> putAction
    ) throws Exception {
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

        PutMetricDataResponse response = putAction.apply(request);
        Assert.assertNotNull(response);
    }

    @Test
    public void testCWMultipleDimentionsAndMetricsAsync() throws Exception {
        final CloudWatchAsyncClient clientCWAsync = TestUtils.getClientCloudWatchAsyncV2();
        validateCWMultipleDimentionsAndMetrics(
            putReq -> clientCWAsync.putMetricData(putReq).get()
        );
    }

    @Test
    public void testCWMultipleDimentionsAndMetrics() throws Exception {
        final CloudWatchClient clientCW = TestUtils.getClientCloudWatchV2();
        validateCWMultipleDimentionsAndMetrics(
            putReq -> clientCW.putMetricData(putReq)
        );
    }

    protected static void validateCWMultipleDimentionsAndMetrics(
        ThrowingFunction<PutMetricDataRequest, PutMetricDataResponse> putAction
    ) throws Exception {
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

        List<MetricDatum> metrics = new ArrayList<>();
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

        PutMetricDataResponse response = putAction.apply(request);
        Assert.assertNotNull(response);
    }

    @Test
    public void testLambdaCreateListFunctionsAsync() throws Exception {
        val lambdaClientAsync = TestUtils.getClientLambdaAsyncV2();
        validateLambdaCreateListFunctions(
            createReq -> lambdaClientAsync.createFunction(createReq).get(),
            x -> lambdaClientAsync.listFunctions().get()
        );
    }

    @Test
    public void testLambdaCreateListFunctions() throws Exception {
        val lambdaClient = TestUtils.getClientLambdaV2();
        validateLambdaCreateListFunctions(
            createReq -> lambdaClient.createFunction(createReq),
            x -> lambdaClient.listFunctions()
        );
    }

    protected static void validateLambdaCreateListFunctions(
        ThrowingFunction<CreateFunctionRequest, CreateFunctionResponse> createAction,
        ThrowingFunction<Void, ListFunctionsResponse> listAction
    ) throws Exception {
        val functionName = "test-f-"+UUID.randomUUID().toString();
        val createFunctionRequest = CreateFunctionRequest.builder().functionName(functionName)
                .runtime(Runtime.JAVA8)
                .role("r1")
                .code(LocalTestUtilSDKV2.createFunctionCode(LambdaHandler.class))
                .handler(LambdaHandler.class.getName()).build();
        val response = createAction.apply(createFunctionRequest);
        Assert.assertNotNull(response);
        val functions = listAction.apply(null);
        val function = functions.functions().stream().filter(f -> f.functionName().equals(functionName)).findFirst().get();
        Assert.assertNotNull(function);
    }

    @Test
	public void testIAMUserCreationAsync() throws Exception {
		IamAsyncClient iamClientAsync = TestUtils.getClientIamAsyncV2();
		validateIAMUserCreation(
		    createReq -> iamClientAsync.createUser(createReq).get(),
		    x -> iamClientAsync.listUsers().get()
	    );
	}

    @Test
    public void testIAMUserCreation() throws Exception {
        IamClient iamClient = TestUtils.getClientIamV2();
        validateIAMUserCreation(
            createReq -> iamClient.createUser(createReq),
            x -> iamClient.listUsers()
        );
    }

    protected static void validateIAMUserCreation(
        ThrowingFunction<CreateUserRequest, CreateUserResponse> createAction,
        ThrowingFunction<Void, ListUsersResponse> listAction
    ) throws Exception {

        String username =  UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = CreateUserRequest.builder().userName(username).build();
        createAction.apply(createUserRequest);

        boolean userFound = false;
        List<User> users = listAction.apply(null).users();

        for (int i = 0; i < users.size(); i++) {
            if(users.get(i).userName().equals(username)){
                userFound = true;
                break;
            }
        }

        Assert.assertTrue(userFound);
    }

    @Test
    public void testIAMListUserPaginationAsync() throws Exception {
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

	@Test
	public void testS3ObjectDeletion() {
		S3AsyncClient s3 = TestUtils.getClientS3AsyncV2();

		String bucketName = UUID.randomUUID().toString();
        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder().bucket(bucketName).build();
		s3.createBucket(createBucketRequest).join();

		String keyName = "my-key-1";        
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucketName).key(keyName).build();
        AsyncRequestBody requestBody = AsyncRequestBody.fromBytes("data".getBytes());
		s3.putObject(objectRequest, requestBody).join();        

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(keyName).build();
		s3.deleteObject(deleteObjectRequest).join();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(keyName).build();
		
		CompletionException exception = assertThrows(CompletionException.class, () -> {
			s3.getObject(getObjectRequest, AsyncResponseTransformer.toBytes()).join();
	    });
		Assert.assertTrue(exception.getCause().getMessage().contains("The specified key does not exist."));			

		s3.putObject(objectRequest, requestBody).join();        
		s3.deleteObject(deleteObjectRequest).join();
		
        CompletionException exception2 = assertThrows(CompletionException.class, () -> {
			s3.getObject(getObjectRequest, AsyncResponseTransformer.toBytes()).join();
	    });
		Assert.assertTrue(exception2.getCause().getMessage().contains("The specified key does not exist."));			
	}

}
