package cloud.localstack.awssdkv2;

import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.*;
import static org.mockito.Mockito.when;

import software.amazon.awssdk.core.client.builder.SdkAsyncClientBuilder;
import software.amazon.awssdk.services.cloudwatch.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.*;
import software.amazon.awssdk.services.sns.*;
import software.amazon.awssdk.services.sqs.*;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.ssm.*;
import cloud.localstack.LocalstackTestRunner;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(LocalstackTestRunner.class)
@PrepareForTest({ KinesisAsyncClient.class, DynamoDbAsyncClient.class, SqsAsyncClient.class, SnsAsyncClient.class,
        SsmAsyncClient.class, SecretsManagerAsyncClient.class, S3AsyncClient.class, CloudWatchAsyncClient.class })
@PowerMockIgnore({ "javax.crypto.*", "org.hamcrest.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "javax.xml.*",
        "org.xml.*", "javax.management.*", "javax.security.*", "org.w3c.*" })
public abstract class PowerMockLocalStack {

    public static void mockKinesisAsync() {
        KinesisAsyncClient mockService = TestUtils.getClientKinesisAsyncV2();
        PowerMockito.mockStatic(KinesisAsyncClient.class);
        when(KinesisAsyncClient.create()).thenReturn(mockService);
    }

    public static void mockDynamoDBAsync() {
        DynamoDbAsyncClient mockService = TestUtils.getClientDyanamoAsyncV2();
        PowerMockito.mockStatic(DynamoDbAsyncClient.class);
        when(DynamoDbAsyncClient.create()).thenReturn(mockService);
    }

    public static void mockSQSAsyncClient() {
        SqsAsyncClient mockService = TestUtils.getClientSQSAsyncV2();
        PowerMockito.mockStatic(SqsAsyncClient.class);
        when(SqsAsyncClient.create()).thenReturn(mockService);
    }

    public static void mockSSMAsyncClient() {
        SsmAsyncClient mockService = TestUtils.getClientSSMAsyncV2();
        PowerMockito.mockStatic(SsmAsyncClient.class);
        when(SsmAsyncClient.create()).thenReturn(mockService);
    }

    public static void mockSecretsManagerAsyncClient() {
        SecretsManagerAsyncClient mockService = TestUtils.getClientSecretsManagerAsyncV2();
        PowerMockito.mockStatic(SecretsManagerAsyncClient.class);
        when(SecretsManagerAsyncClient.create()).thenReturn(mockService);
    }

    public static void mockS3AsyncClient() {
        S3AsyncClient mockService = TestUtils.getClientS3AsyncV2();
        PowerMockito.mockStatic(S3AsyncClient.class);
        when(S3AsyncClient.create()).thenReturn(mockService);
    }

    public static void mockCloudWatchAsyncClient() {
        CloudWatchAsyncClient mockService = TestUtils.getClientCloudWatchAsyncV2();
        PowerMockito.mockStatic(CloudWatchAsyncClient.class);
        when(CloudWatchAsyncClient.create()).thenReturn(mockService);
    }

}
