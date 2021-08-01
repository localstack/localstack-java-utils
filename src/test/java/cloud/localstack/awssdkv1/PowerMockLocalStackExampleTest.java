package cloud.localstack.awssdkv1;

import javax.jms.JMSException;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cloud.localstack.awssdkv1.PowerMockLocalStack;

/**
 * Test integration of SES messaging with LocalStack
 */
public class PowerMockLocalStackExampleTest extends PowerMockLocalStack{

    private static final String TOPIC = "topic";

    @Before
    public void mock() {
        PowerMockLocalStack.mockSNS();
    }

    @Test
    public void testSendMessage() throws JMSException {
        final AmazonSNS clientSNS = AmazonSNSClientBuilder.defaultClient();
        final CreateTopicResult createTopicResult = clientSNS.createTopic(TOPIC);
        final PublishResult publishResult = clientSNS.publish(createTopicResult.getTopicArn(), "message");
        Assert.assertNotNull(publishResult);
    }
}
