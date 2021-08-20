package cloud.localstack.awssdkv2;

import cloud.localstack.awssdkv2.consumer.DeliveryStatusRecordProcessorFactory;
import cloud.localstack.awssdkv2.consumer.EventProcessor;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import io.thundra.jexter.junit4.core.sysprop.SystemPropertySandboxRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.CreateStreamResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.metrics.NullMetricsFactory;
import software.amazon.kinesis.retrieval.RetrievalConfig;
import software.amazon.kinesis.retrieval.polling.PollingConfig;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@LocalstackDockerProperties(ignoreDockerRunErrors = true)
public class KinesisSchedulerTest extends PowerMockLocalStack {

    String streamName = "test" + UUID.randomUUID().toString();
    String workerId = UUID.randomUUID().toString();
    String testMessage = "hello, world";
    Integer consumerCreationTime = 15; //35 for real AWS

    // Revert system properties to the back after the test
    @Rule
    public SystemPropertySandboxRule systemPropertySandboxRule = new SystemPropertySandboxRule();

    @Before
    public void mockServicesForScheduler() {
        System.setProperty(SdkSystemSetting.CBOR_ENABLED.property(), "false");
        PowerMockLocalStack.mockCloudWatchAsyncClient();
        PowerMockLocalStack.mockDynamoDBAsync();
        PowerMockLocalStack.mockKinesisAsync();
    }

    @Test
    public void schedulerTest() throws Exception {
        KinesisAsyncClient kinesisAsyncClient = KinesisAsyncClient.create();
        DynamoDbAsyncClient dynamoAsyncClient = DynamoDbAsyncClient.create();
        CloudWatchAsyncClient cloudWatchAsyncClient = CloudWatchAsyncClient.create();

        createStream(kinesisAsyncClient);
        TimeUnit.SECONDS.sleep(2);

        EventProcessor eventProcessor = new EventProcessor();
        DeliveryStatusRecordProcessorFactory processorFactory = new DeliveryStatusRecordProcessorFactory(eventProcessor);

        ConfigsBuilder configsBuilder = new ConfigsBuilder(streamName, streamName, kinesisAsyncClient, dynamoAsyncClient,
                cloudWatchAsyncClient, workerId, processorFactory) {
            @Override
            public RetrievalConfig retrievalConfig() {
                RetrievalConfig retrievalConfig = super.retrievalConfig();
                retrievalConfig.retrievalSpecificConfig(new PollingConfig(streamName(), kinesisClient()));
                return retrievalConfig;
            }
        };
        Scheduler scheduler = createScheduler(configsBuilder);

        new Thread(scheduler).start();
        TimeUnit.SECONDS.sleep(consumerCreationTime);

        putRecord(kinesisAsyncClient);
        TimeUnit.SECONDS.sleep(5);

        scheduler.shutdown();
        Assert.assertTrue(eventProcessor.CONSUMER_CREATED);
        Assert.assertTrue(eventProcessor.RECORD_RECEIVED);
        Assert.assertTrue(eventProcessor.messages.size() > 0);
        Assert.assertEquals(eventProcessor.messages.get(0), testMessage);
    }

    public Scheduler createScheduler(ConfigsBuilder configsBuilder) {
        return new Scheduler(configsBuilder.checkpointConfig(), configsBuilder.coordinatorConfig(),
            configsBuilder.leaseManagementConfig(), configsBuilder.lifecycleConfig(),
            configsBuilder.metricsConfig().metricsFactory(new NullMetricsFactory()), configsBuilder.processorConfig(),
            configsBuilder.retrievalConfig());
    }

    public void createStream(KinesisAsyncClient kinesisClient) throws Exception {
        CreateStreamRequest request = CreateStreamRequest.builder().streamName(streamName).shardCount(1).build();
        CreateStreamResponse response = kinesisClient.createStream(request).get();
        Assert.assertNotNull(response);
    }

    public void putRecord(KinesisAsyncClient kinesisClient) throws Exception {
        PutRecordRequest request = PutRecordRequest.builder().partitionKey("partitionkey").streamName(streamName)
            .data(SdkBytes.fromUtf8String(testMessage)).build();
        PutRecordResponse response = kinesisClient.putRecord(request).get();
        Assert.assertNotNull(response);
    }

}
