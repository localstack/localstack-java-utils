package cloud.localstack.awssdkv2;

import java.util.UUID;

import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cloud.localstack.Constants;

/**
 * Test integration of SES messaging with LocalStack
 */
public class PowerMockLocalStackExampleTest extends PowerMockLocalStack{

    @Before
    public void mock() {
        PowerMockLocalStack.mockSQSAsyncClient();
    }
    
    @Test
    public void testCreateSqsQueueV2() throws Exception {
        String queueName = "test-q-"+ UUID.randomUUID().toString();
        CreateQueueRequest request = CreateQueueRequest.builder().queueName(queueName).build();
        SqsAsyncClient sqsClient = SqsAsyncClient.create();
        CreateQueueResponse queue = sqsClient.createQueue(request).get();
        Assert.assertTrue(queue.queueUrl().contains(Constants.DEFAULT_AWS_ACCOUNT_ID + "/" + queueName));
    }
}
