package cloud.localstack.awssdkv2;

import cloud.localstack.Localstack;
import cloud.localstack.awssdkv2.consumer.DeliveryStatusRecordProcessorFactory;
import cloud.localstack.awssdkv2.consumer.EventProcessor;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.CreateStreamResponse;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.metrics.NullMetricsFactory;
import software.amazon.kinesis.coordinator.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

@LocalstackDockerProperties(ignoreDockerRunErrors = true)
public class KinesisSchedulerTest extends PowerMockLocalStack {
  String streamName = "test"+UUID.randomUUID().toString();
  String workerId = UUID.randomUUID().toString();
  Boolean mocked = false;

  @Before
  public void mockServicesForScheduler() {
    // System.setProperty(SdkSystemSetting.CBOR_ENABLED.property(), "false");
    PowerMockLocalStack.mockCloudWatchAsyncClient();
    PowerMockLocalStack.mockDynamoDBAsync();
    PowerMockLocalStack.mockKinesisAsync();
  }

  @Test
  public void schedulerTest() throws Exception {

    createStream();

    KinesisAsyncClient kinesisAsyncClient = KinesisAsyncClient.create();
    DynamoDbAsyncClient dynamoAsyncClient = DynamoDbAsyncClient.create();
    CloudWatchAsyncClient cloudWatchAsyncClient = CloudWatchAsyncClient.create();

    EventProcessor eventProcessor = new EventProcessor();
    DeliveryStatusRecordProcessorFactory processorFactory = new DeliveryStatusRecordProcessorFactory(eventProcessor);

    ConfigsBuilder configsBuilder = new ConfigsBuilder(streamName, streamName, kinesisAsyncClient, dynamoAsyncClient,
        cloudWatchAsyncClient, workerId, processorFactory);

    Scheduler scheduler = createScheduler(configsBuilder);
    scheduler.run();
    TimeUnit.SECONDS.sleep(10);
    scheduler.shutdown();
  }

  public Scheduler createScheduler(ConfigsBuilder configsBuilder) {
    return new Scheduler(configsBuilder.checkpointConfig(), configsBuilder.coordinatorConfig(),
        configsBuilder.leaseManagementConfig(), configsBuilder.lifecycleConfig(),
        configsBuilder.metricsConfig().metricsFactory(new NullMetricsFactory()), configsBuilder.processorConfig(),
        configsBuilder.retrievalConfig());
  }

  public void createStream() throws Exception {
    KinesisAsyncClient kinesisClient = KinesisAsyncClient.create();

    CreateStreamRequest request = CreateStreamRequest.builder().streamName(streamName).shardCount(1).build();

    CreateStreamResponse response = kinesisClient.createStream(request).get();
    Assert.assertNotNull(response);
    TimeUnit.SECONDS.sleep(2);
  }

}