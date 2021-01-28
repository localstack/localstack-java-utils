package cloud.localstack;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.awssdkv1.TestUtils;

import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.model.CreateStreamRequest;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.GetRecordsRequest;
import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.GetShardIteratorRequest;
import com.amazonaws.SDKGlobalConfiguration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.nio.ByteBuffer;

@RunWith(LocalstackTestRunner.class)
public class KinesisConsumerTest {

  @Test
  public void testGetRecordCBOR() throws Exception {
    String streamName = "test-s-" + UUID.randomUUID().toString();
    AmazonKinesisAsync kinesisClient = TestUtils.getClientKinesisAsync();

    CreateStreamRequest createStreamRequest = new CreateStreamRequest();
    createStreamRequest.setStreamName(streamName);
    createStreamRequest.setShardCount(1);

    kinesisClient.createStream(createStreamRequest);
    TimeUnit.SECONDS.sleep(2);

    PutRecordRequest putRecordRequest = new PutRecordRequest();
    putRecordRequest.setPartitionKey("partitionkey");
    putRecordRequest.setStreamName(streamName);

    String message = "Hello world!";
    putRecordRequest.setData(ByteBuffer.wrap(message.getBytes()));

    String shardId = kinesisClient.putRecord(putRecordRequest).getShardId();

    GetShardIteratorRequest getShardIteratorRequest = new GetShardIteratorRequest();
    getShardIteratorRequest.setShardId(shardId);
    getShardIteratorRequest.setShardIteratorType("TRIM_HORIZON");
    getShardIteratorRequest.setStreamName(streamName);

    String shardIterator = kinesisClient.getShardIterator(getShardIteratorRequest).getShardIterator();

    GetRecordsRequest getRecordRequest = new GetRecordsRequest();
    getRecordRequest.setShardIterator(shardIterator);

    getRecordRequest.setShardIterator(shardIterator);
    GetRecordsResult recordsResponse = kinesisClient.getRecords(getRecordRequest);

    List<String> records = recordsResponse.getRecords().stream().map(r -> new String(r.getData().array()))
        .collect(Collectors.toList());
    Assert.assertEquals(message, records.get(0));
  }

  @Test
  public void testGetRecordJSON() throws Exception {
    System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");
    this.testGetRecordCBOR();
    System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "false");
  }
}