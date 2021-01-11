package cloud.localstack.awssdkv1;

import cloud.localstack.LocalstackTestRunner;

import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.model.CreateStreamRequest;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesis.model.GetRecordsRequest;
import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.GetShardIteratorRequest;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.internal.SdkInternalList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.lang.System.out;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;

@RunWith(LocalstackTestRunner.class)
public class KinesisConsumerTest {

    @Test
    public void testGetRecord() throws Exception{
      String streamName = "test-s-"+UUID.randomUUID().toString();
      AmazonKinesisAsync kinesisClient = TestUtils.getClientKinesisAsync();

      CreateStreamRequest createStreamRequest = new CreateStreamRequest();
      createStreamRequest.setStreamName(streamName);
      createStreamRequest.setShardCount(1);

      kinesisClient.createStream(createStreamRequest);
      TimeUnit.SECONDS.sleep(2);

      PutRecordRequest putRecordRequest = new PutRecordRequest();
      putRecordRequest.setPartitionKey("partitionkey");
      putRecordRequest.setStreamName(streamName);
      putRecordRequest.setData(ByteBuffer.wrap("hello, world!".getBytes()));

      String shardId = kinesisClient.putRecord(putRecordRequest).getShardId();

      GetShardIteratorRequest getShardIteratorRequest = new GetShardIteratorRequest();
      getShardIteratorRequest.setShardId(shardId);
      getShardIteratorRequest.setShardIteratorType("TRIM_HORIZON");
      getShardIteratorRequest.setStreamName(streamName);

      String shardIterator = kinesisClient
        .getShardIterator(getShardIteratorRequest)
        .getShardIterator();

      GetRecordsRequest getRecordRequest = new GetRecordsRequest() ;
      getRecordRequest.setShardIterator(shardIterator);

      Integer limit = 100;
      Integer counter = 0;
      Boolean recordFound = false; 
      
      while (true) {
        getRecordRequest.setShardIterator(shardIterator);
        GetRecordsResult recordsResponse = kinesisClient.getRecords(getRecordRequest);
        
        List records = recordsResponse.getRecords();
        if (records.isEmpty()) {
          recordFound = true; 
          break;
        }

        if(counter >= limit){
          break;
        }

        counter += 1;
        shardIterator = recordsResponse.getNextShardIterator();
      }
      Assert.assertTrue(recordFound);
    }
}