package cloud.localstack.awssdkv1;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.client.builder.ExecutorFactory;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreams;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreamsClientBuilder;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementAsync;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementAsyncClientBuilder;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSAsync;
import com.amazonaws.services.kms.AWSKMSAsyncClientBuilder;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsAsync;
import com.amazonaws.services.logs.AWSLogsAsyncClientBuilder;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import cloud.localstack.Constants;
import cloud.localstack.Localstack;
import cloud.localstack.CommonUtils;

@SuppressWarnings("all")
public class TestUtils {

    public static final AWSCredentials TEST_CREDENTIALS = new BasicAWSCredentials(
        Constants.TEST_ACCESS_KEY, Constants.TEST_SECRET_KEY);

    public static AmazonSQS getClientSQS() {
        return getClientSQS(null);
    }

    public static AmazonSQS getClientSQS(String endpoint) {
        endpoint = endpoint == null ? Localstack.INSTANCE.getEndpointSQS() : endpoint;
        return AmazonSQSClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfiguration(endpoint)).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AmazonSQSAsync getClientSQSAsync() {
        return AmazonSQSAsyncClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationSQS()).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AmazonSQSAsync getClientSQSAsync(final ExecutorFactory executorFactory) {
        return AmazonSQSAsyncClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationSQS()).
                withExecutorFactory(executorFactory).
                withCredentials(getCredentialsProvider()).build();
    }
    
    public static AmazonSimpleEmailService getClientSES() {
        return AmazonSimpleEmailServiceClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationSES()).
                withCredentials(getCredentialsProvider()).build();
    }
    
    public static AmazonSimpleEmailServiceAsync getClientSESAsync() {
        return AmazonSimpleEmailServiceAsyncClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationSNS()).
                withCredentials(getCredentialsProvider()).build();
    }
    
    public static AmazonSNS getClientSNS() {
        return AmazonSNSClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationSNS()).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AmazonSNSAsync getClientSNSAsync() {
        return AmazonSNSAsyncClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationSNS()).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AmazonSNSAsync getClientSNSAsync(final ExecutorFactory executorFactory) {
        return AmazonSNSAsyncClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationSNS()).
                withExecutorFactory(executorFactory).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AWSLambda getClientLambda() {
        return AWSLambdaClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationLambda()).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AWSLambdaAsync getClientLambdaAsync() {
        return AWSLambdaAsyncClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationLambda()).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AWSLambdaAsync getClientLambdaAsync(final ExecutorFactory executorFactory) {
        return AWSLambdaAsyncClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationLambda()).
                withExecutorFactory(executorFactory).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AmazonS3 getClientS3() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationS3()).
                withCredentials(getCredentialsProvider());
        builder.setPathStyleAccessEnabled(true);
        return builder.build();
    }

    public static AWSSecretsManager getClientSecretsManager() {
        return AWSSecretsManagerClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationSecretsManager()).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AmazonDynamoDB getClientDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(getEndpointConfigurationDynamoDB())
                .withCredentials(getCredentialsProvider()).build();
    }

    public static AmazonDynamoDBStreams getClientDynamoDBStreams() {
        return AmazonDynamoDBStreamsClientBuilder.standard()
                .withEndpointConfiguration(getEndpointConfigurationDynamoDBStreams())
                .withCredentials(getCredentialsProvider()).build();
    }

    public static AmazonKinesis getClientKinesis() {
        return AmazonKinesisClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationKinesis()).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AmazonKinesisAsync getClientKinesisAsync() {
        return AmazonKinesisAsyncClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationKinesis()).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AmazonKinesisAsync getClientKinesisAsync(final ExecutorFactory executorFactory) {
        return AmazonKinesisAsyncClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationKinesis()).
                withExecutorFactory(executorFactory).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AmazonCloudWatch getClientCloudWatch() {
        return AmazonCloudWatchClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationCloudWatch()).
                withCredentials(getCredentialsProvider()).build();
    }
    
    public static AWSLogs getClientCloudWatchLogs() {
        return AWSLogsClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationCloudWatchLogs()).
                withCredentials(getCredentialsProvider()).build();
    }
    
    public static AWSLogsAsync getClientCloudWatchLogsAsync() {
        return AWSLogsAsyncClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationCloudWatchLogs()).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AmazonIdentityManagement getClientIAM() {
        return AmazonIdentityManagementClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationIAM()).
                withCredentials(getCredentialsProvider()).build();
    }
    
    public static AmazonIdentityManagementAsync getClientIAMAsync() {
        return AmazonIdentityManagementAsyncClientBuilder.standard().
                withEndpointConfiguration(getEndpointConfigurationIAM()).
                withCredentials(getCredentialsProvider()).build();
    }

    public static AWSKMS getClientKMS(){
        return AWSKMSClientBuilder.standard().
            withEndpointConfiguration(getEndpointConfigurationKMS()).
            withCredentials(getCredentialsProvider()).build();
    }
    
    public static AWSKMSAsync getClientKMSAsync(){
        return AWSKMSAsyncClientBuilder.standard().
            withEndpointConfiguration(getEndpointConfigurationKMS()).
            withCredentials(getCredentialsProvider()).build();
    }

    protected static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationIAM() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointIAM());
    }

    protected static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationLambda() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointLambda());
    }

    protected static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationKinesis() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointKinesis());
    }

    protected static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationDynamoDB() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointDynamoDB());
    }

    protected static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationDynamoDBStreams() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointDynamoDBStreams());
    }

    protected static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationSQS() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointSQS());
    }

    protected static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationS3() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointS3());
    }
    
    public static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationSES() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointSES());
    }

    protected static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationSNS() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointSNS());
    }

    protected static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationCloudWatch() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointCloudWatch());
    }
    
    private static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationCloudWatchLogs() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointCloudWatchLogs());
	}

    protected static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationSecretsManager() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointSecretsmanager());
    }

    protected static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationStepFunctions() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointStepFunctions());
    }
    
    protected static AwsClientBuilder.EndpointConfiguration getEndpointConfigurationKMS() {
        return getEndpointConfiguration(Localstack.INSTANCE.getEndpointKMS());
    }

    /**
     * UTIL METHODS
     */

    public static AWSCredentialsProvider getCredentialsProvider() {
        return new AWSStaticCredentialsProvider(TEST_CREDENTIALS);
    }

    protected static AwsClientBuilder.EndpointConfiguration getEndpointConfiguration(String endpointURL) {
        return new AwsClientBuilder.EndpointConfiguration(endpointURL, Constants.DEFAULT_REGION);
    }

}
