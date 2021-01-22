package cloud.localstack;

import cloud.localstack.utils.PromiseAsyncHandler;
import cloud.localstack.awssdkv1.TestUtils;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.model.*;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.JMSException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Test integration of SES messaging with LocalStack
 */
@RunWith(LocalstackTestRunner.class)
public class SESMessagingTest {

    static final String FROM = "sender@example.com";
    static final String TO = "recipient@example.com";
    static final String CONFIGSET = "ConfigSet";
    static final String SUBJECT = "Amazon SES test (AWS SDK for Java)";
    static final String HTMLBODY = "<h1>Amazon SES test (AWS SDK for Java)</h1>"
            + "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>"
            + "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>" + "AWS SDK for Java</a>";
    static final String TEXTBODY = "This email was sent through Amazon SES " + "using the AWS SDK for Java.";

       

    @Test
    public void testSendEmail() throws Exception {
        AmazonSimpleEmailService client = TestUtils.getClientSES();

        VerifyEmailAddressRequest verifyEmailAddressRequest = new VerifyEmailAddressRequest().withEmailAddress(TO);
        client.verifyEmailAddress(verifyEmailAddressRequest);
        
        verifyEmailAddressRequest.setEmailAddress(FROM);
        client.verifyEmailAddress(verifyEmailAddressRequest);

        SendEmailRequest request = new SendEmailRequest().withDestination(new Destination().withToAddresses(TO))
                .withMessage(new Message()
                        .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(HTMLBODY))
                                .withText(new Content().withCharset("UTF-8").withData(TEXTBODY)))
                        .withSubject(new Content().withCharset("UTF-8").withData(SUBJECT)))
                .withSource(FROM).withConfigurationSetName(CONFIGSET);
                
        SendEmailResult result = client.sendEmail(request);
        Assert.assertNotNull(result);
    }

    @Test
    public void testSendAsyncEmail() throws Exception {
        AmazonSimpleEmailServiceAsync client = TestUtils.getClientSESAsync();

        VerifyEmailAddressRequest verifyEmailAddressRequest = new VerifyEmailAddressRequest().withEmailAddress(TO);
        client.verifyEmailAddress(verifyEmailAddressRequest);
        
        verifyEmailAddressRequest.setEmailAddress(FROM);
        client.verifyEmailAddress(verifyEmailAddressRequest);

        SendEmailRequest request = new SendEmailRequest().withDestination(new Destination().withToAddresses(TO))
                .withMessage(new Message()
                        .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(HTMLBODY))
                                .withText(new Content().withCharset("UTF-8").withData(TEXTBODY)))
                        .withSubject(new Content().withCharset("UTF-8").withData(SUBJECT)))
                .withSource(FROM).withConfigurationSetName(CONFIGSET);
                
        SendEmailResult result = client.sendEmail(request);
        Assert.assertNotNull(result);
    }
}
