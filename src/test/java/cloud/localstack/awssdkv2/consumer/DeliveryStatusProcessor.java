package cloud.localstack.awssdkv2.consumer;

import java.io.IOException;


import software.amazon.kinesis.lifecycle.events.InitializationInput;
import software.amazon.kinesis.lifecycle.events.LeaseLostInput;
import software.amazon.kinesis.lifecycle.events.ProcessRecordsInput;
import software.amazon.kinesis.lifecycle.events.ShardEndedInput;
import software.amazon.kinesis.lifecycle.events.ShutdownRequestedInput;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

public class DeliveryStatusProcessor implements ShardRecordProcessor{
    EventProcessor eventProcessor;

    public DeliveryStatusProcessor(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    public void initialize(InitializationInput initializationInput) {
        // System.out.println("EVENT PROCESSOR: initialize");
        this.eventProcessor.CONSUMER_CREATED = true;
    }

    public void processRecords(ProcessRecordsInput processRecordsInput) {
        // System.out.println("EVENT PROCESSOR: process records");
        this.eventProcessor.RECORD_RECEIVED = true;
    }

    public void processRecord(KinesisClientRecord record) throws IOException {
        // System.out.println("EVENT PROCESSOR: process record");
        this.eventProcessor.RECORD_RECEIVED = true;
    }

    public void processAndPublishRecord(byte[] messageStatus) throws IOException {
        // System.out.println("EVENT PROCESSOR: process and publish record");
    }

    public void leaseLost(LeaseLostInput leaseLostInput) {
        // System.out.println("EVENT PROCESSOR: lease Lost");
    }

    public void shardEnded(ShardEndedInput shardEndedInput) {
        // System.out.println("EVENT PROCESSOR: shard ended");
    }

    public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
        // System.out.println("EVENT PROCESSOR: shutdown requested");
    }
}
