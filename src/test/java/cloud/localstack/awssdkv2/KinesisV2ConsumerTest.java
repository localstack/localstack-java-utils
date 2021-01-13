package cloud.localstack.awssdkv2;

import cloud.localstack.LocalstackTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.Protocol;
import software.amazon.awssdk.utils.AttributeMap;
import software.amazon.awssdk.http.nio.netty.*;
import software.amazon.awssdk.core.*;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.*;

import static java.lang.System.out;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RunWith(LocalstackTestRunner.class)
public class KinesisV2ConsumerTest {

    @Test
    public void testGetRecord() throws Exception{
      String streamName = "test-s-"+UUID.randomUUID().toString();
      KinesisAsyncClient kinesisClient = TestUtils.getClientKinesisAsyncV2();

      CreateStreamRequest request = CreateStreamRequest.builder()
          .streamName(streamName).shardCount(1).build();
      CreateStreamResponse response = kinesisClient.createStream(request).get();
      Assert.assertNotNull(response);
      TimeUnit.SECONDS.sleep(2);

      PutRecordRequest putRecordRequest = PutRecordRequest.builder()
        .partitionKey("partitionkey")
        .streamName(streamName)
        .data(SdkBytes.fromUtf8String("hello, world!"))
        .build();
      String shardId = kinesisClient.putRecord(putRecordRequest).get().shardId();

      GetShardIteratorRequest getShardIteratorRequest = GetShardIteratorRequest.builder()
        .shardId(shardId)
        .shardIteratorType(ShardIteratorType.TRIM_HORIZON)
        .streamName(streamName)
        .build();
      String shardIterator = kinesisClient
        .getShardIterator(getShardIteratorRequest)
        .get()
        .shardIterator();

      GetRecordsRequest getRecordRequest = GetRecordsRequest.builder().shardIterator(shardIterator).build();
      Integer limit = 100;
      Integer counter = 0;
      Boolean recordFound = false;
      
      while (true) {
        GetRecordsResponse recordsResponse = kinesisClient.getRecords(getRecordRequest).get();
        
        if (recordsResponse.hasRecords()) {
          recordFound = true;
          break;
        }

        if(counter >= limit){
          break;
        }

        counter += 1;
        shardIterator = recordsResponse.nextShardIterator();
      }
      Assert.assertTrue(recordFound);
    }
}