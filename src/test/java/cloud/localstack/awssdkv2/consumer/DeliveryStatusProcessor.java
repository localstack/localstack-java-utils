package cloud.localstack.awssdkv2.consumer;

import java.io.IOException;
import java.util.logging.Logger;

import software.amazon.kinesis.lifecycle.events.InitializationInput;
import software.amazon.kinesis.lifecycle.events.LeaseLostInput;
import software.amazon.kinesis.lifecycle.events.ProcessRecordsInput;
import software.amazon.kinesis.lifecycle.events.ShardEndedInput;
import software.amazon.kinesis.lifecycle.events.ShutdownRequestedInput;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

public class DeliveryStatusProcessor implements ShardRecordProcessor {
    EventProcessor eventProcessor;
    private static final Logger LOG = Logger.getLogger(DeliveryStatusProcessor.class.getName());

    public DeliveryStatusProcessor(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    public void initialize(InitializationInput initializationInput) {
        this.eventProcessor.CONSUMER_CREATED = true;
    }

    public void processRecords(ProcessRecordsInput processRecordsInput) {
        LOG.info("RECORDS PROCESSING");
        this.eventProcessor.RECORD_RECEIVED = true;
        processRecordsInput.records().forEach(record -> {
            try {
                processRecord(record);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void processRecord(KinesisClientRecord record) throws IOException {
        LOG.info("Processing record: " + record);
        this.eventProcessor.RECORD_RECEIVED = true;
        byte[] message = new byte[record.data().remaining()];
        record.data().get(message);
        String string = new String(message);
        eventProcessor.messages.add(string);
    }

    public void processAndPublishRecord(byte[] messageStatus) throws IOException {
    }

    public void leaseLost(LeaseLostInput leaseLostInput) {
    }

    public void shardEnded(ShardEndedInput shardEndedInput) {
    }

    public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
    }
}
