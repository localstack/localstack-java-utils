package cloud.localstack.awssdkv2;

import cloud.localstack.awssdkv2.TestUtils;
import cloud.localstack.LocalstackTestRunner;

import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.services.sqs.*;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.kinesis.*;
import software.amazon.awssdk.services.kinesis.model.*;
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
        // TODO fix classpath for v2 and finalize test
        System.out.println(queue);
    }

    @Test
    public void testCreateKinesisStreamV2() throws Exception {
        String streamName = "test-s-3198";
        KinesisAsyncClient kinesisClient = TestUtils.getClientKinesisAsyncV2();
        CreateStreamRequest request = CreateStreamRequest.builder()
            .streamName(streamName).shardCount(1).build();
        CreateStreamResponse response = kinesisClient.createStream(request).get();
        // TODO fix classpath for v2 and finalize test
        System.out.println(response);
    }

}
