package cloud.localstack.awssdkv2;

import cloud.localstack.awssdkv2.TestUtils;
import cloud.localstack.LocalstackTestRunner;

import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.services.kinesis.*;
import software.amazon.awssdk.services.kinesis.model.*;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.sns.*;
import software.amazon.awssdk.services.sns.model.*;
import software.amazon.awssdk.services.sqs.*;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;
import java.net.*;

@RunWith(LocalstackTestRunner.class)
public class BasicFeaturesSDKV2Test {

    static {
        System.setProperty(SdkSystemSetting.CBOR_ENABLED.property(), "false");
    }

    @Test
    public void testCreateSqsQueueV2() throws Exception {
        String queueName = "test-q-2159";
        CreateQueueRequest request = CreateQueueRequest.builder().queueName(queueName).build();
        SqsAsyncClient sqsClient = TestUtils.getClientSQSAsyncV2();
        CreateQueueResponse queue = sqsClient.createQueue(request).get();
        Assert.assertTrue(queue.queueUrl().contains("queue/" + queueName));
    }

    @Test
    public void testCreateKinesisStreamV2() throws Exception {
        String streamName = "test-s-3198";
        KinesisAsyncClient kinesisClient = TestUtils.getClientKinesisAsyncV2();
        CreateStreamRequest request = CreateStreamRequest.builder()
            .streamName(streamName).shardCount(1).build();
        CreateStreamResponse response = kinesisClient.createStream(request).get();
        Assert.assertNotNull(response);
    }

    @Test
    public void testS3CreateListBuckets() throws Exception {
        String bucketName = "test-b-9716";
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

        final String topicName = "test-t-6210";
        final SnsAsyncClient clientSNS = TestUtils.getClientSNSAsyncV2();
        CreateTopicResponse createTopicResponse = clientSNS.createTopic(
            CreateTopicRequest.builder().name(topicName).build()).get();

        String topicArn = createTopicResponse.topicArn();
        Assert.assertNotNull(topicArn);
        PublishRequest publishRequest = PublishRequest.builder().topicArn(topicArn).subject("test subject").message("message test.").build();

        PublishResponse publishResponse = clientSNS.publish(publishRequest).get();
        Assert.assertNotNull(publishResponse.messageId());
    }
}
