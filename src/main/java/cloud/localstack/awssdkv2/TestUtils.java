package cloud.localstack.awssdkv2;

import cloud.localstack.Localstack;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.core.client.builder.SdkAsyncClientBuilder;
import software.amazon.awssdk.core.client.builder.SdkSyncClientBuilder;
import software.amazon.awssdk.http.SdkHttpConfigurationOption;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.iam.IamAsyncClient;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.qldb.QldbAsyncClient;
import software.amazon.awssdk.services.qldb.QldbClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.utils.AttributeMap;

import java.net.URI;

/**
 * Utility methods for AWS SDK v2
 */
@SuppressWarnings("all")
public class TestUtils {

    public static KinesisAsyncClient getClientKinesisAsyncV2() {
        return wrapApiAsyncClientV2(KinesisAsyncClient.builder(), Localstack.INSTANCE.getEndpointKinesis()).build();
    }

    public static KinesisClient getClientKinesisV2() {
        return wrapApiSyncClientV2(KinesisClient.builder(), Localstack.INSTANCE.getEndpointKinesis()).build();
    }

    public static DynamoDbAsyncClient getClientDyanamoAsyncV2() {
        return wrapApiAsyncClientV2(DynamoDbAsyncClient.builder(), Localstack.INSTANCE.getEndpointDynamoDB()).build();
    }

    public static DynamoDbClient getClientDyanamoV2() {
        return wrapApiSyncClientV2(DynamoDbClient.builder(), Localstack.INSTANCE.getEndpointDynamoDB()).build();
    }

    public static SqsAsyncClient getClientSQSAsyncV2() {
        return wrapApiAsyncClientV2(SqsAsyncClient.builder(), Localstack.INSTANCE.getEndpointSQS()).build();
    }

    public static SqsClient getClientSQSV2() {
        return wrapApiSyncClientV2(SqsClient.builder(), Localstack.INSTANCE.getEndpointSQS()).build();
    }

    public static QldbAsyncClient getClientQLDBAsyncV2() {
        return wrapApiAsyncClientV2(QldbAsyncClient.builder(), Localstack.INSTANCE.getEndpointQLDB()).build();
    }

    public static QldbClient getClientQLDBV2() {
        return wrapApiSyncClientV2(QldbClient.builder(), Localstack.INSTANCE.getEndpointQLDB()).build();
    }

    public static SnsAsyncClient getClientSNSAsyncV2() {
        return wrapApiAsyncClientV2(SnsAsyncClient.builder(), Localstack.INSTANCE.getEndpointSNS()).build();
    }

    public static SnsClient getClientSNSV2() {
        return wrapApiSyncClientV2(SnsClient.builder(), Localstack.INSTANCE.getEndpointSNS()).build();
    }

    public static SsmAsyncClient getClientSSMAsyncV2() {
        return wrapApiAsyncClientV2(SsmAsyncClient.builder(), Localstack.INSTANCE.getEndpointSSM()).build();
    }

    public static SsmClient getClientSSMV2() {
        return wrapApiSyncClientV2(SsmClient.builder(), Localstack.INSTANCE.getEndpointSSM()).build();
    }

    public static SecretsManagerAsyncClient getClientSecretsManagerAsyncV2() {
        return wrapApiAsyncClientV2(SecretsManagerAsyncClient.builder(), Localstack.INSTANCE.getEndpointSSM()).build();
    }

    public static SecretsManagerClient getClientSecretsManagerV2() {
        return wrapApiSyncClientV2(SecretsManagerClient.builder(), Localstack.INSTANCE.getEndpointSSM()).build();
    }

    public static S3AsyncClient getClientS3AsyncV2() {
        return wrapApiAsyncClientV2(S3AsyncClient.builder(), Localstack.INSTANCE.getEndpointS3()).build();
    }

    public static S3Client getClientS3V2() {
        return wrapApiSyncClientV2(S3Client.builder(), Localstack.INSTANCE.getEndpointS3()).build();
    }

    public static CloudWatchAsyncClient getClientCloudWatchAsyncV2() {
        return wrapApiAsyncClientV2(CloudWatchAsyncClient.builder(), Localstack.INSTANCE.getEndpointCloudWatch()).build();
    }

    public static CloudWatchClient getClientCloudWatchV2() {
        return wrapApiSyncClientV2(CloudWatchClient.builder(), Localstack.INSTANCE.getEndpointCloudWatch()).build();
    }

    public static LambdaAsyncClient getClientLambdaAsyncV2() {
        return wrapApiAsyncClientV2(LambdaAsyncClient.builder(), Localstack.INSTANCE.getEndpointLambda()).build();
    }

    public static LambdaClient getClientLambdaV2() {
        return wrapApiSyncClientV2(LambdaClient.builder(), Localstack.INSTANCE.getEndpointLambda()).build();
    }

    public static IamAsyncClient getClientIamAsyncV2() {
        return wrapApiAsyncClientV2(IamAsyncClient.builder(), Localstack.INSTANCE.getEndpointIAM()).build();
    }

    public static IamClient getClientIamV2() {
        return wrapApiSyncClientV2(IamClient.builder(), Localstack.INSTANCE.getEndpointIAM()).build();
    }

    public static <T extends SdkAsyncClientBuilder> T wrapApiAsyncClientV2(T builder, String endpointURL) {
        try {
            return (T) ((AwsClientBuilder)builder
                .httpClient(NettyNioAsyncHttpClient.builder().buildWithDefaults(
                    AttributeMap.builder().put(
                        SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES, java.lang.Boolean.TRUE).build())))
                .credentialsProvider(getCredentialsV2())
                .region(Region.of(Localstack.INSTANCE.getDefaultRegion()))
                .endpointOverride(new URI(endpointURL));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends SdkSyncClientBuilder> T wrapApiSyncClientV2(T builder, String endpointURL) {
        try {
            return (T) ((AwsClientBuilder)builder
                .httpClient(ApacheHttpClient.builder().buildWithDefaults(
                    AttributeMap.builder().put(
                        SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES, java.lang.Boolean.TRUE).build())))
                .credentialsProvider(getCredentialsV2())
                .region(Region.of(Localstack.INSTANCE.getDefaultRegion()))
                .endpointOverride(new URI(endpointURL));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static AwsCredentialsProvider getCredentialsV2() throws Exception {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create("access", "secret"));
    }

}
