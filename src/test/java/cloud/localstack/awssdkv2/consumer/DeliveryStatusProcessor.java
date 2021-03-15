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

    public DeliveryStatusProcessor(EventProcessor eventProcessor) {
    }

    public void initialize(InitializationInput initializationInput) {
    }

    public void processRecords(ProcessRecordsInput processRecordsInput) {
    }

    public void processRecord(KinesisClientRecord record) throws IOException {
    }

    public void processAndPublishRecord(byte[] messageStatus) throws IOException {
    }

    @Override
    public void leaseLost(LeaseLostInput leaseLostInput) {
    }

    public void shardEnded(ShardEndedInput shardEndedInput) {
    }

    public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
    }
}
