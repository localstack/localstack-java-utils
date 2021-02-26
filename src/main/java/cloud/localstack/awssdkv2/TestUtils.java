package cloud.localstack.awssdkv2;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.utils.*;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.services.cloudwatch.*;
import software.amazon.awssdk.services.kinesis.*;
import software.amazon.awssdk.services.sns.*;
import software.amazon.awssdk.services.sqs.*;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.ssm.*;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.core.client.builder.SdkAsyncClientBuilder;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;

import cloud.localstack.Localstack;

import java.net.*;

/**
 * Utility methods for AWS SDK v2
 */
@SuppressWarnings("all")
public class TestUtils {

    public static KinesisAsyncClient getClientKinesisAsyncV2() {
        return wrapApiClientV2(KinesisAsyncClient.builder(), Localstack.INSTANCE.getEndpointKinesis()).build();
    }

    public static DynamoDbAsyncClient getClientDyanamoAsyncV2() {
        return wrapApiClientV2(DynamoDbAsyncClient.builder(), Localstack.INSTANCE.getEndpointDynamoDB()).build();
    }

    public static SqsAsyncClient getClientSQSAsyncV2() {
        return wrapApiClientV2(SqsAsyncClient.builder(), Localstack.INSTANCE.getEndpointSQS()).build();
    }

    public static SnsAsyncClient getClientSNSAsyncV2() {
        return wrapApiClientV2(SnsAsyncClient.builder(), Localstack.INSTANCE.getEndpointSNS()).build();
    }

    public static SsmAsyncClient getClientSSMAsyncV2() {
        return wrapApiClientV2(SsmAsyncClient.builder(), Localstack.INSTANCE.getEndpointSSM()).build();
    }

    public static SecretsManagerAsyncClient getClientSecretsManagerAsyncV2() {
        return wrapApiClientV2(SecretsManagerAsyncClient.builder(), Localstack.INSTANCE.getEndpointSSM()).build();
    }

    public static S3AsyncClient getClientS3AsyncV2() {
        return wrapApiClientV2(S3AsyncClient.builder(), Localstack.INSTANCE.getEndpointS3()).build();
    }
    
    public static CloudWatchAsyncClient getClientCloudWatchAsyncV2() {
        return wrapApiClientV2(CloudWatchAsyncClient.builder(), Localstack.INSTANCE.getEndpointCloudWatch()).build();
    }

    public static LambdaAsyncClient getClientLambdaAsyncV2() {
        return wrapApiClientV2(LambdaAsyncClient.builder(), Localstack.INSTANCE.getEndpointCloudWatch()).build();
    }

    public static <T extends SdkAsyncClientBuilder> T wrapApiClientV2(T builder, String endpointURL) {
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

    private static AwsCredentialsProvider getCredentialsV2() throws Exception {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create("access", "secret"));
    }

}
