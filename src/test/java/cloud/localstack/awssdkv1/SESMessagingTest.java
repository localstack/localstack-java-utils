package cloud.localstack.awssdkv1;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.awssdkv1.TestUtils;

import java.net.URI;
import java.util.UUID;

import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.model.*;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.CreateTemplateResponse;
import software.amazon.awssdk.services.ses.model.GetSendStatisticsResponse;
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailResponse;

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

    @Test
    public void testSendTemplatedEmailV2() throws Throwable {
        SesClient client = SesClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("access", "secret")))
                .region(Region.of(Localstack.INSTANCE.getDefaultRegion()))
                .endpointOverride(new URI(Localstack.INSTANCE.getEndpointSES()))
                .build();

        software.amazon.awssdk.services.ses.model.VerifyEmailAddressRequest verifyEmailAddressRequestTo = software.amazon.awssdk.services.ses.model.VerifyEmailAddressRequest.builder().emailAddress(TO).build();
        client.verifyEmailAddress(verifyEmailAddressRequestTo);

        software.amazon.awssdk.services.ses.model.VerifyEmailAddressRequest verifyEmailAddressRequestFrom = software.amazon.awssdk.services.ses.model.VerifyEmailAddressRequest.builder().emailAddress(FROM).build();
        client.verifyEmailAddress(verifyEmailAddressRequestFrom);

        try {
            this.testCreateTemplateV2(client);
        } catch (Exception e) {
            throw new Throwable("Error creating template to send");
        }

        String templateData = "{ \"name\":\"Alejandro\", \"favoriteanimal\": \"alligator\" }";

        software.amazon.awssdk.services.ses.model.SendTemplatedEmailRequest request = software.amazon.awssdk.services.ses.model.SendTemplatedEmailRequest.builder()
                .configurationSetName(CONFIGSET)
                .source(FROM)
                .destination(software.amazon.awssdk.services.ses.model.Destination.builder().toAddresses(TO).build())
                .template(templateName)
                .templateData(templateData)
                .build();

        SendTemplatedEmailResponse result = client.sendTemplatedEmail(request);

        // this call is throwing exception
        GetSendStatisticsResponse statistics = client.getSendStatistics();

        Assert.assertNotNull(result);
    }

    private void testCreateTemplateV2(SesClient client) {
        templateName = "test-s-" + UUID.randomUUID().toString();
        String subjectPart = "Greetings, {{name}}!";
        String htmlPart = "<h1>Hello {{name}},</h1><p>Your favorite animal is {{favoriteanimal}}.</p>";
        String textPart = "Dear {{name}},\r\nYour favorite animal is {{favoriteanimal}}.";

        software.amazon.awssdk.services.ses.model.Template template = software.amazon.awssdk.services.ses.model.Template.builder()
                .htmlPart(htmlPart)
                .textPart(textPart)
                .subjectPart(subjectPart)
                .templateName(templateName)
                .build();

        software.amazon.awssdk.services.ses.model.CreateTemplateRequest request = software.amazon.awssdk.services.ses.model.CreateTemplateRequest.builder().template(template).build();
        CreateTemplateResponse result = client.createTemplate(request);

        Assert.assertNotNull(result);
    }

}
