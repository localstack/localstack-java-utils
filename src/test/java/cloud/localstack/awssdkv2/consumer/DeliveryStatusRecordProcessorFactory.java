package cloud.localstack.awssdkv2.consumer;

import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

public class DeliveryStatusRecordProcessorFactory implements ShardRecordProcessorFactory {
    private final EventProcessor eventProcessor;

    public DeliveryStatusRecordProcessorFactory(EventProcessor eventProcessor) {
      this.eventProcessor = eventProcessor;
    }

    public ShardRecordProcessor shardRecordProcessor() {
      return new DeliveryStatusProcessor(eventProcessor);
    }
}
