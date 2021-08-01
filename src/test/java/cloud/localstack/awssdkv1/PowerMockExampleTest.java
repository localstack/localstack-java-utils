package cloud.localstack.awssdkv1;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.awssdkv1.TestUtils;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;

import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.jms.JMSException;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.*;

/**
 * Test integration of SES messaging with LocalStack
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(LocalstackTestRunner.class)
@LocalstackDockerProperties(services = { "sns" })
@PrepareForTest({ AmazonSNS.class, AmazonSNSClientBuilder.class })
@PowerMockIgnore({"javax.crypto.*", "org.hamcrest.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*", "javax.security.*", "org.w3c.*"})
public class PowerMockExampleTest {
    private static final String TOPIC = "topic";

    @Before
    public void mock() {
        AmazonSNS mockSes = TestUtils.getClientSNS();
        PowerMockito.mockStatic(AmazonSNSClientBuilder.class);
        when(AmazonSNSClientBuilder.defaultClient()).thenReturn(mockSes);
    }


    @Test
    public void testSendMessage() throws JMSException {
        final AmazonSNS clientSNS = AmazonSNSClientBuilder.defaultClient();
        final CreateTopicResult createTopicResult = clientSNS.createTopic(TOPIC);
        final PublishResult publishResult = clientSNS.publish(createTopicResult.getTopicArn(), "message");
        Assert.assertNotNull(publishResult);
    }

}
