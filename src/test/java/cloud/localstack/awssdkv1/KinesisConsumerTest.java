package cloud.localstack.awssdkv1;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.model.CreateStreamRequest;
import com.amazonaws.services.kinesis.model.GetRecordsRequest;
import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.GetShardIteratorRequest;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.ResourceInUseException;
import io.thundra.jexter.junit4.core.sysprop.SystemPropertySandboxRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(LocalstackTestRunner.class)
@LocalstackDockerProperties(ignoreDockerRunErrors=true)
public class KinesisConsumerTest {

    // Revert system properties to the back after the test
    @Rule
    public SystemPropertySandboxRule systemPropertySandboxRule = new SystemPropertySandboxRule();

    @Test
    public void testGetRecordCBOR() throws Exception {
        System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "false");
        runGetRecord();
    }

    @Test
    public void testGetRecordJSON() throws Exception {
        System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");
        runGetRecord();
    }

    private void runGetRecord() throws Exception {
        String streamName = "test-s-" + UUID.randomUUID().toString();
        AmazonKinesisAsync kinesisClient = TestUtils.getClientKinesisAsync();

        try {
            CreateStreamRequest createStreamRequest = new CreateStreamRequest();
            createStreamRequest.setStreamName(streamName);
            createStreamRequest.setShardCount(1);

            kinesisClient.createStream(createStreamRequest);
            TimeUnit.SECONDS.sleep(1);
        } catch (ResourceInUseException e) { /* ignore */ }

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

}