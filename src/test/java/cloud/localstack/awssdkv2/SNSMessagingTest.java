package cloud.localstack.awssdkv2;

import cloud.localstack.LocalstackTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.concurrent.ExecutionException;

/**
 * Test integration of SNS messaging with LocalStack using SDK v2
 */
@RunWith(LocalstackTestRunner.class)
public class SNSMessagingTest {
    private static final String TOPIC = "topic";

    @Test
    public void testSendMessage() throws ExecutionException, InterruptedException {
        final SnsAsyncClient clientSNS = TestUtils.getClientSNSAsyncV2();
        CreateTopicResponse createTopicResponse = clientSNS.createTopic(CreateTopicRequest.builder().name(TOPIC).build()).get();

        String topicArn = createTopicResponse.topicArn();
        Assert.assertNotNull(topicArn);
        PublishRequest publishRequest = PublishRequest.builder().topicArn(topicArn).subject("test subject").message("message test.").build();

        PublishResponse publishResponse = clientSNS.publish(publishRequest).get();
        Assert.assertNotNull(publishResponse.messageId());
    }
}
