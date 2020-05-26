package cloud.localstack;

import cloud.localstack.utils.PromiseAsyncHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import javax.jms.JMSException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Test integration of SNS messaging with LocalStack
 */
@RunWith(LocalstackTestRunner.class)
public class SNSMessagingTest {
    private static final String TOPIC = "topic";

    @Test
    public void testSendMessage() {
        final AmazonSNS clientSNS = TestUtils.getClientSNS();
        final CreateTopicResult createTopicResult = clientSNS.createTopic(TOPIC);
        final PublishResult publishResult = clientSNS.publish(createTopicResult.getTopicArn(), "message");
        Assert.assertNotNull(publishResult);
    }

    @Test
    public void testSendMessageV2() {
        final SnsClient clientSNS = TestUtils.getSNSClientV2();
        CreateTopicResponse rs = clientSNS.createTopic(software.amazon.awssdk.services.sns.model.CreateTopicRequest.builder().name("topic1").build());
        String topicArn = rs.topicArn();
        PublishResponse publishResponse = clientSNS.publish(software.amazon.awssdk.services.sns.model.PublishRequest.builder()
                .topicArn(topicArn)
                .subject("Test Subject")
                .message("Hello world.")
                .build());

        Assert.assertNotNull(publishResponse);
    }

    @Test
    public void testSendMessageAsync() throws Exception {
        final AmazonSNSAsync clientSNSAsync = TestUtils.getClientSNSAsync();
        final PromiseAsyncHandler<CreateTopicRequest, CreateTopicResult> createTopicPromise = new PromiseAsyncHandler<>();
        clientSNSAsync.createTopicAsync(TOPIC, createTopicPromise);

        final CompletableFuture<PublishResult> publishResult = createTopicPromise.thenCompose(createTopicResult -> {
            final PromiseAsyncHandler<PublishRequest, PublishResult> publishPromise = new PromiseAsyncHandler<>();
            clientSNSAsync.publishAsync(createTopicResult.getTopicArn(), "message", publishPromise);
            return publishPromise;
        });

        final PublishResult result = publishResult.get(3, TimeUnit.SECONDS);
        Assert.assertNotNull(result);
    }
}