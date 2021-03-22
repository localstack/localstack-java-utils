package cloud.localstack.awssdkv2.consumer;

import java.util.ArrayList;
import java.util.List;

public class EventProcessor {
    public Boolean CONSUMER_CREATED = false;
    public Boolean RECORD_RECEIVED = false;
    public List<String> messages = new ArrayList<>();
}
