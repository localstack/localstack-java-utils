package cloud.localstack.awssdkv2;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import io.thundra.jexter.junit4.core.sysprop.SystemPropertySandboxRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.CreateStreamResponse;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(LocalstackTestRunner.class)
@LocalstackDockerProperties(ignoreDockerRunErrors = true)
public class KinesisV2ConsumerTest {

  // Revert system properties to the back after the test suite (class)
  @Rule
  public SystemPropertySandboxRule systemPropertySandboxRule = new SystemPropertySandboxRule();

  @Test
  public void testGetRecordCBOR() throws Exception {
    System.setProperty(SdkSystemSetting.CBOR_ENABLED.property(), "true");

    String streamName = "test-s-" + UUID.randomUUID().toString();
    KinesisAsyncClient kinesisClient = TestUtils.getClientKinesisAsyncV2();

    CreateStreamRequest request = CreateStreamRequest.builder().streamName(streamName).shardCount(1).build();
    CreateStreamResponse response = kinesisClient.createStream(request).get();
    Assert.assertNotNull(response);
    TimeUnit.SECONDS.sleep(2);

    String message = "hello, world!";
    PutRecordRequest putRecordRequest = PutRecordRequest.builder().partitionKey("partitionkey").streamName(streamName)
        .data(SdkBytes.fromUtf8String(message)).build();
    String shardId = kinesisClient.putRecord(putRecordRequest).get().shardId();

    GetShardIteratorRequest getShardIteratorRequest = GetShardIteratorRequest.builder().shardId(shardId)
        .shardIteratorType(ShardIteratorType.TRIM_HORIZON).streamName(streamName).build();
    String shardIterator = kinesisClient.getShardIterator(getShardIteratorRequest).get().shardIterator();

    GetRecordsRequest getRecordRequest = GetRecordsRequest.builder().shardIterator(shardIterator).build();
    GetRecordsResponse recordsResponse = kinesisClient.getRecords(getRecordRequest).get();
    List<String> records = recordsResponse.records().stream().map(r -> new String(r.data().asUtf8String()))
        .collect(Collectors.toList());

    Assert.assertEquals(message, records.get(0));
  }

  @Test
  public void testGetRecordJSON() throws Exception {
    System.setProperty(SdkSystemSetting.CBOR_ENABLED.property(), "false");
    testGetRecordCBOR();
  }

}