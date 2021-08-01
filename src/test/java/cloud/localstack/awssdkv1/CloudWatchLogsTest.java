package cloud.localstack.awssdkv1;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.model.CreateLogGroupRequest;
import com.amazonaws.services.logs.model.CreateLogStreamRequest;
import com.amazonaws.services.logs.model.DescribeLogGroupsResult;
import com.amazonaws.services.logs.model.GetLogEventsRequest;
import com.amazonaws.services.logs.model.GetLogEventsResult;
import com.amazonaws.services.logs.model.InputLogEvent;
import com.amazonaws.services.logs.model.LogGroup;
import com.amazonaws.services.logs.model.OutputLogEvent;
import com.amazonaws.services.logs.model.PutLogEventsRequest;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;

/**
 * Test service usage of CloudWatchLogs with example connection, creation of log group stream
 * and manual publication of sample events and their basic retrieval
 *
 * Issue: https://github.com/localstack/localstack-java-utils/issues/11
 *
 * @author Andrew Duffy
 */
@RunWith(LocalstackTestRunner.class)
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = {"logs"}, ignoreDockerRunErrors=true)
public class CloudWatchLogsTest {

    @org.junit.Test
    @org.junit.jupiter.api.Test
		public void testLogGroupSetupAndPublish() {
				AWSLogs cloudWatchLogs = TestUtils.getClientCloudWatchLogs();
				DescribeLogGroupsResult groups = cloudWatchLogs.describeLogGroups();
				Assertions.assertTrue(groups.getLogGroups().isEmpty());

				String logGroupName = createLogGroup();

				DescribeLogGroupsResult groupsAfterCreation = cloudWatchLogs.describeLogGroups();
				Assertions.assertFalse(groupsAfterCreation.getLogGroups().isEmpty());
				Assertions.assertEquals(1, groupsAfterCreation.getLogGroups().size());

				LogGroup newGroup = groupsAfterCreation.getLogGroups().get(0);
				Assertions.assertEquals(logGroupName, newGroup.getLogGroupName());
				Assertions.assertNotNull(newGroup.getArn());
				Assertions.assertEquals(0, newGroup.getStoredBytes());

				String logStream = createLogStream(logGroupName);

				List<InputLogEvent> events = publishLogEvents(logGroupName, logStream);

				GetLogEventsResult publishedEvents = fetchEvents(logGroupName, logStream);
				Assertions.assertEquals(events.size(), publishedEvents.getEvents().size());

				List<String> messagesSent = events.stream().map(InputLogEvent::getMessage).collect(Collectors.toList());
				for (OutputLogEvent publishedOutputEvent: publishedEvents.getEvents()) {
					Assertions.assertTrue(messagesSent.contains(publishedOutputEvent.getMessage()));
				}
		}

		public String createLogGroup() {
				CreateLogGroupRequest createLogGroupRequest = new CreateLogGroupRequest();
				createLogGroupRequest.setLogGroupName("testLogGroupName-" + UUID.randomUUID().toString());

				TestUtils.getClientCloudWatchLogs().createLogGroup(createLogGroupRequest);
				return createLogGroupRequest.getLogGroupName();
		}

		public String createLogStream(String newGroup) {
				CreateLogStreamRequest newStreamRequest = new CreateLogStreamRequest();
				newStreamRequest.setLogGroupName(newGroup);
				newStreamRequest.setLogStreamName("stream-" + UUID.randomUUID().toString());

				TestUtils.getClientCloudWatchLogs().createLogStream(newStreamRequest);
				return newStreamRequest.getLogStreamName();
		}

		public List<InputLogEvent> publishLogEvents(String groupName, String logStream) {
				InputLogEvent event1 = new InputLogEvent();
				event1.setMessage("Event1-" + UUID.randomUUID().toString());
				event1.setTimestamp(System.currentTimeMillis());

				InputLogEvent event2 = new InputLogEvent();
				event2.setMessage("Event2-" + UUID.randomUUID().toString());
				event2.setTimestamp(System.currentTimeMillis());
				List<InputLogEvent> events = new ArrayList<>();
				events.add(event1);
				events.add(event2);

				PutLogEventsRequest putLogEventsRequest = new PutLogEventsRequest();
				putLogEventsRequest.setLogGroupName(groupName);
				putLogEventsRequest.setLogStreamName(logStream);
				putLogEventsRequest.setLogEvents(events);

				TestUtils.getClientCloudWatchLogs().putLogEvents(putLogEventsRequest);
				return events;
		}

		public GetLogEventsResult fetchEvents(String groupName, String logStream) {
				GetLogEventsRequest getLogEventsRequest = new GetLogEventsRequest();
				getLogEventsRequest.setLogStreamName(logStream);
				getLogEventsRequest.setLogGroupName(groupName);

				return TestUtils.getClientCloudWatchLogs().getLogEvents(getLogEventsRequest);
		}

}
