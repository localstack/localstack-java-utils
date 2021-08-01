package cloud.localstack.awssdkv1;

import cloud.localstack.awssdkv1.lambda.KinesisEventParser;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static cloud.localstack.LambdaExecutor.get;
import static cloud.localstack.LambdaExecutor.readFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("unchecked")
public class KinesisEventMappingTest {

    @Test
    public void testKinesisRecord() throws Exception {
        String fileContent = readFile("src/test/resources/KinesisEventLambda.json");
        ObjectMapper reader = new ObjectMapper();
        reader.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        reader.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Map<String,Object> map = reader.readerFor(Map.class).readValue(fileContent);

        List<Map<String,Object>> records = (List<Map<String, Object>>) get(map, "Records");

        KinesisEvent inputObject = KinesisEventParser.parse(records);
        assertNotNull(inputObject);
        assertEquals(1, inputObject.getRecords().size());

        KinesisEvent.KinesisEventRecord record = inputObject.getRecords().get(0);
        assertEquals("us-east-2", record.getAwsRegion());
        assertEquals("aws:kinesis", record.getEventSource());
        assertEquals("shardId-000000000006:49590338271490256608559692538361571095921575989136588898",
                record.getEventID());
        assertEquals("arn:aws:kinesis:us-east-2:123456789012:stream/lambda-stream", record.getEventSourceARN());
        assertEquals("aws:kinesis:record", record.getEventName());
        assertEquals("arn:aws:iam::123456789012:role/lambda-role", record.getInvokeIdentityArn());

        KinesisEvent.Record kinesisRecord = record.getKinesis();
        assertEquals("1.0", kinesisRecord.getKinesisSchemaVersion());
        assertEquals("1", kinesisRecord.getPartitionKey());
        assertEquals("49590338271490256608559692538361571095921575989136588898",
                kinesisRecord.getSequenceNumber());
        assertEquals(ByteBuffer.wrap("Hello, this is a test.".getBytes()), kinesisRecord.getData());
        assertNotNull(kinesisRecord.getApproximateArrivalTimestamp());
    }
}
