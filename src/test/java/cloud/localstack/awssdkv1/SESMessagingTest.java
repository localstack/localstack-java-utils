package cloud.localstack.awssdkv1;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.awssdkv1.TestUtils;

import java.util.UUID;

import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.model.*;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test integration of SES messaging with LocalStack
 */
@RunWith(LocalstackTestRunner.class)
@LocalstackDockerProperties(ignoreDockerRunErrors = true)
public class SESMessagingTest {

    static final String FROM = "sender@example.com";
    static final String TO = "recipient@example.com";
    static final String CONFIGSET = "ConfigSet";
    static final String SUBJECT = "Amazon SES test (AWS SDK for Java)";
    static final String HTMLBODY = "<h1>Amazon SES test (AWS SDK for Java)</h1>"
            + "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>"
            + "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>" + "AWS SDK for Java</a>";
    static final String TEXTBODY = "This email was sent through Amazon SES " + "using the AWS SDK for Java.";

    private String templateName = "";

    @Test
    public void testSendEmail() throws Exception {
        AmazonSimpleEmailService client = TestUtils.getClientSES();

        VerifyEmailAddressRequest verifyEmailAddressRequest = new VerifyEmailAddressRequest().withEmailAddress(TO);
        client.verifyEmailAddress(verifyEmailAddressRequest);

        verifyEmailAddressRequest.setEmailAddress(FROM);
        client.verifyEmailAddress(verifyEmailAddressRequest);

        Message message = new Message()
            .withBody(new Body()
            .withHtml(new Content().withCharset("UTF-8").withData(HTMLBODY))
            .withText(new Content().withCharset("UTF-8").withData(TEXTBODY)))
            .withSubject(new Content().withCharset("UTF-8").withData(SUBJECT));

        SendEmailRequest request = new SendEmailRequest()
            .withSource(FROM).withConfigurationSetName(CONFIGSET)
            .withDestination(new Destination().withToAddresses(TO))
            .withMessage(message);

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

        Message message = new Message()
            .withBody(new Body()
            .withHtml(new Content().withCharset("UTF-8").withData(HTMLBODY))
            .withText(new Content().withCharset("UTF-8").withData(TEXTBODY)))
            .withSubject(new Content().withCharset("UTF-8").withData(SUBJECT));

        SendEmailRequest request = new SendEmailRequest()
            .withSource(FROM).withConfigurationSetName(CONFIGSET)
            .withDestination(new Destination().withToAddresses(TO))
            .withMessage(message);

        SendEmailResult result = client.sendEmail(request);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateTemplate() throws Exception {
        AmazonSimpleEmailService client = TestUtils.getClientSES();

        templateName = "test-s-" + UUID.randomUUID().toString();
        String subjectPart= "Greetings, {{name}}!";
        String htmlPart= "<h1>Hello {{name}},</h1><p>Your favorite animal is {{favoriteanimal}}.</p>";
        String textPart= "Dear {{name}},\r\nYour favorite animal is {{favoriteanimal}}.";

        Template template = new Template()
            .withHtmlPart(htmlPart)
            .withTextPart(textPart)
            .withSubjectPart(subjectPart)
            .withTemplateName(templateName);

        CreateTemplateRequest request = new CreateTemplateRequest().withTemplate(template);
        CreateTemplateResult result = client.createTemplate(request);

        Assert.assertNotNull(result);
    }

    @Test
    public void testSendTemplatedEmail() throws Throwable {
        AmazonSimpleEmailService client = TestUtils.getClientSES();

        VerifyEmailAddressRequest verifyEmailAddressRequest = new VerifyEmailAddressRequest().withEmailAddress(TO);
        client.verifyEmailAddress(verifyEmailAddressRequest);

        verifyEmailAddressRequest.setEmailAddress(FROM);
        client.verifyEmailAddress(verifyEmailAddressRequest);

        try {
            this.testCreateTemplate();
        } catch (Exception e) {
            throw new Throwable("Error creating template to send");
        }

        String templateData = "{ \"name\":\"Alejandro\", \"favoriteanimal\": \"alligator\" }";

        SendTemplatedEmailRequest request = new SendTemplatedEmailRequest()
        .withConfigurationSetName(CONFIGSET)
        .withSource(FROM)
        .withDestination(new Destination().withToAddresses(TO))
        .withTemplate(templateName)
        .withTemplateData(templateData);

        SendTemplatedEmailResult result = client.sendTemplatedEmail(request);
        Assert.assertNotNull(result);
    }
}
