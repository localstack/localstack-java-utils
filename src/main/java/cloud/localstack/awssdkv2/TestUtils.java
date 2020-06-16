package cloud.localstack.awssdkv2;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.*;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.services.kinesis.*;
import software.amazon.awssdk.services.sns.*;
import software.amazon.awssdk.services.sqs.*;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;

import cloud.localstack.Localstack;

import java.net.*;

@SuppressWarnings("all")
public class TestUtils {

    /**
     * AWS SDK V2 METHODS
     */

    public static KinesisAsyncClient getClientKinesisAsyncV2() {
        return wrapApiClientV2(KinesisAsyncClient.builder(), Localstack.INSTANCE.getEndpointKinesis()).build();
    }

    public static SqsAsyncClient getClientSQSAsyncV2() {
        return wrapApiClientV2(SqsAsyncClient.builder(), Localstack.INSTANCE.getEndpointSQS()).build();
    }

    public static SnsAsyncClient getClientSNSAsyncV2() {
        return wrapApiClientV2(SnsAsyncClient.builder(), Localstack.INSTANCE.getEndpointSNS()).build();
    }

    public static <T extends software.amazon.awssdk.core.client.builder.SdkAsyncClientBuilder> T wrapApiClientV2(T builder, String endpointURL) {
        try {
            return (T) ((software.amazon.awssdk.awscore.client.builder.AwsClientBuilder)builder
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

    private static software.amazon.awssdk.auth.credentials.AwsCredentialsProvider getCredentialsV2() throws Exception {
        return software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create(
            software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create("access", "secret"));
    }

}
