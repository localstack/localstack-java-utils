package cloud.localstack.awssdkv1.lambda;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.Record;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static cloud.localstack.LambdaExecutor.get;

public class KinesisEventParser {

    public static KinesisEvent parse(List<Map<String, Object>> records) {
        KinesisEvent kinesisEvent = new KinesisEvent();
        kinesisEvent.setRecords(new LinkedList<>());
        for (Map<String, Object> record : records) {
            KinesisEventRecord r = new KinesisEventRecord();
            kinesisEvent.getRecords().add(r);

            r.setEventSourceARN((String) get(record, "eventSourceARN"));
            r.setEventSource((String) get(record, "eventSource"));
            r.setEventName((String) get(record, "eventName"));
            r.setEventVersion((String) get(record, "eventVersion"));
            r.setEventID((String) get(record, "eventID"));
            r.setAwsRegion((String) get(record, "awsRegion"));
            r.setInvokeIdentityArn((String) get(record, "invokeIdentityArn"));

            //Kinesis
            Map<String, Object> kinesis = (Map<String, Object>) get(record, "Kinesis");
            String dataString = new String(get(kinesis, "Data").toString().getBytes());
            byte[] decodedData = Base64.getDecoder().decode(dataString);
            Record kinesisRecord = new Record();
            kinesisRecord.setData(ByteBuffer.wrap(decodedData));
            kinesisRecord.setPartitionKey((String) get(kinesis, "PartitionKey"));
            kinesisRecord.setSequenceNumber((String) get(kinesis, "SequenceNumber"));
            kinesisRecord.setKinesisSchemaVersion((String) get(kinesis, "KinesisSchemaVersion"));
            kinesisRecord.setApproximateArrivalTimestamp(new Date());
            r.setKinesis(kinesisRecord);
        }

        return kinesisEvent;
    }
}
