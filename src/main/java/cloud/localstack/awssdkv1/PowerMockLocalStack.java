package cloud.localstack.awssdkv1;

import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.*;
import static org.mockito.Mockito.when;

import com.amazonaws.client.builder.ExecutorFactory;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreams;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreamsClientBuilder;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementAsync;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementAsyncClientBuilder;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSAsync;
import com.amazonaws.services.kms.AWSKMSAsyncClientBuilder;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsAsync;
import com.amazonaws.services.logs.AWSLogsAsyncClientBuilder;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import cloud.localstack.LocalstackTestRunner;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(LocalstackTestRunner.class)
@PrepareForTest({ AmazonCloudWatchClientBuilder.class, AmazonDynamoDBClientBuilder.class,
        AmazonDynamoDBStreamsClientBuilder.class, AmazonIdentityManagementClientBuilder.class,
        AmazonKinesisAsyncClientBuilder.class, AmazonKinesisClientBuilder.class, AWSLambdaAsyncClientBuilder.class,
        AWSLambdaClientBuilder.class, AWSLogsClientBuilder.class, AWSLogsAsyncClientBuilder.class,
        AmazonS3ClientBuilder.class, AmazonSimpleEmailServiceAsyncClientBuilder.class,
        AmazonSimpleEmailServiceClientBuilder.class, AmazonSNSAsyncClientBuilder.class, AmazonSNSClientBuilder.class,
        AWSKMSClientBuilder.class, AWSKMSAsyncClientBuilder.class,
        AWSSecretsManagerClientBuilder.class, AmazonSQSClientBuilder.class })
@PowerMockIgnore({ "javax.crypto.*", "org.hamcrest.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "javax.xml.*",
        "org.xml.*", "javax.management.*", "javax.security.*", "org.w3c.*" })
public abstract class PowerMockLocalStack {

    public static void mockCloudWatch() {
        AmazonCloudWatch mockService = TestUtils.getClientCloudWatch();
        PowerMockito.mockStatic(AmazonCloudWatchClientBuilder.class);
        when(AmazonCloudWatchClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockDynamoDB() {
        AmazonDynamoDB mockService = TestUtils.getClientDynamoDB();
        PowerMockito.mockStatic(AmazonDynamoDBClientBuilder.class);
        when(AmazonDynamoDBClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockDynamoDBStreams() {
        AmazonDynamoDBStreams mockService = TestUtils.getClientDynamoDBStreams();
        PowerMockito.mockStatic(AmazonDynamoDBStreamsClientBuilder.class);
        when(AmazonDynamoDBStreamsClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockIdentityManager() {
        AmazonIdentityManagement mockService = TestUtils.getClientIAM();
        PowerMockito.mockStatic(AmazonIdentityManagementClientBuilder.class);
        when(AmazonIdentityManagementClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockIdentityManagerAsync() {
        AmazonIdentityManagementAsync mockServiceAsync = TestUtils.getClientIAMAsync();
        PowerMockito.mockStatic(AmazonIdentityManagementAsyncClientBuilder.class);
        when(AmazonIdentityManagementAsyncClientBuilder.defaultClient()).thenReturn(mockServiceAsync);
    }

    public static void mockKinesis() {
        AmazonKinesis mockService = TestUtils.getClientKinesis();
        PowerMockito.mockStatic(AmazonKinesisClientBuilder.class);
        when(AmazonKinesisClientBuilder.defaultClient()).thenReturn(mockService);
    }
    
    public static void mockKMS() {
        AWSKMS mockService = TestUtils.getClientKMS();
        PowerMockito.mockStatic(AWSKMSClientBuilder.class);
        when(AWSKMSClientBuilder.defaultClient()).thenReturn(mockService);
    }
    
    public static void mockKMSAsync() {
        AWSKMSAsync mockService = TestUtils.getClientKMSAsync();
        PowerMockito.mockStatic(AWSKMSAsyncClientBuilder.class);
        when(AWSKMSAsyncClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockKinesisAsync() {
        AmazonKinesisAsync mockServiceAsync = TestUtils.getClientKinesisAsync();
        PowerMockito.mockStatic(AmazonKinesisAsyncClientBuilder.class);
        when(AmazonKinesisAsyncClientBuilder.defaultClient()).thenReturn(mockServiceAsync);
    }

    public static void mockKinesisAsync(final ExecutorFactory executorFactory) {
        AmazonKinesisAsync mockServiceAsync = TestUtils
                .getClientKinesisAsync(executorFactory);
        PowerMockito.mockStatic(AmazonKinesisAsyncClientBuilder.class);
        when(AmazonKinesisAsyncClientBuilder.defaultClient()).thenReturn(mockServiceAsync);
    }

    public static void mockLambda() {
        AWSLambda mockService = TestUtils.getClientLambda();
        PowerMockito.mockStatic(AWSLambdaClientBuilder.class);
        when(AWSLambdaClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockLambdaAsync() {
        AWSLambdaAsync mockService = TestUtils.getClientLambdaAsync();
        PowerMockito.mockStatic(AWSLambdaAsyncClientBuilder.class);
        when(AWSLambdaAsyncClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockLambdaAsync(final ExecutorFactory executorFactory) {
        AWSLambdaAsync mockService = TestUtils.getClientLambdaAsync(executorFactory);
        PowerMockito.mockStatic(AWSLambdaAsyncClientBuilder.class);
        when(AWSLambdaAsyncClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockLogs() {
        AWSLogs mockService = TestUtils.getClientCloudWatchLogs();
        PowerMockito.mockStatic(AWSLogsClientBuilder.class);
        when(AWSLogsClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockLogsAsync() {
        AWSLogsAsync mockService = TestUtils.getClientCloudWatchLogsAsync();
        PowerMockito.mockStatic(AWSLogsAsyncClientBuilder.class);
        when(AWSLogsAsyncClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockS3() {
        AmazonS3 mockS3 = TestUtils.getClientS3();
        PowerMockito.mockStatic(AmazonS3ClientBuilder.class);
        when(AmazonS3ClientBuilder.defaultClient()).thenReturn(mockS3);
    }
    
    public static void mockSecretsManager() {
        AWSSecretsManager mock = TestUtils.getClientSecretsManager();
        PowerMockito.mockStatic(AWSSecretsManagerClientBuilder.class);
        when(AWSSecretsManagerClientBuilder.defaultClient()).thenReturn(mock);
    }

    public static void mockSES() {
        AmazonSimpleEmailService mockSes = TestUtils.getClientSES();
        PowerMockito.mockStatic(AmazonSimpleEmailServiceClientBuilder.class);
        when(AmazonSimpleEmailServiceClientBuilder.defaultClient()).thenReturn(mockSes);
    }

    public static void mockSESAsync() {
        AmazonSimpleEmailServiceAsync mockSesAsync = TestUtils.getClientSESAsync();
        PowerMockito.mockStatic(AmazonSimpleEmailServiceAsyncClientBuilder.class);
        when(AmazonSimpleEmailServiceAsyncClientBuilder.defaultClient()).thenReturn(mockSesAsync);
    }

    public static void mockSNS() {
        AmazonSNS mockService = TestUtils.getClientSNS();
        PowerMockito.mockStatic(AmazonSNSClientBuilder.class);
        when(AmazonSNSClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockSNSAsync() {
        AmazonSNSAsync mockService = TestUtils.getClientSNSAsync();
        PowerMockito.mockStatic(AmazonSNSAsyncClientBuilder.class);
        when(AmazonSNSAsyncClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockSNSAsync(final ExecutorFactory executorFactory) {
        AmazonSNSAsync mockService = TestUtils.getClientSNSAsync(executorFactory);
        PowerMockito.mockStatic(AmazonSNSAsyncClientBuilder.class);
        when(AmazonSNSAsyncClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockSQS() {
        AmazonSQS mockService = TestUtils.getClientSQS();
        PowerMockito.mockStatic(AmazonSQSClientBuilder.class);
        when(AmazonSQSClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockSQS(String endpoint) {
        AmazonSQS mockService = TestUtils.getClientSQS(endpoint);
        PowerMockito.mockStatic(AmazonSQSClientBuilder.class);
        when(AmazonSQSClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockSQSAsync() {
        AmazonSQSAsync mockService = TestUtils.getClientSQSAsync();
        PowerMockito.mockStatic(AmazonSQSAsyncClientBuilder.class);
        when(AmazonSQSAsyncClientBuilder.defaultClient()).thenReturn(mockService);
    }

    public static void mockSQSAsync(final ExecutorFactory executorFactory) {
        AmazonSQSAsync mockService = TestUtils.getClientSQSAsync(executorFactory);
        PowerMockito.mockStatic(AmazonSQSAsyncClientBuilder.class);
        when(AmazonSQSAsyncClientBuilder.defaultClient()).thenReturn(mockService);
    }
}
