package cloud.localstack.awssdkv2;

import java.util.*;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import software.amazon.awssdk.services.emr.EmrClient;
import software.amazon.awssdk.services.emr.model.*;

@RunWith(LocalstackTestRunner.class)
@LocalstackDockerProperties(ignoreDockerRunErrors = true)
public class EMRJobFlowTest {
    public static List<Application> getStandardApplications() {
        return Arrays.asList(
                Application.builder().name("Ganglia").version("3.7.2").build(),
                Application.builder().name("Hive").version("2.3.7").build(),
                Application.builder().name("Livy").version("0.7.0").build(),
                Application.builder().name("Spark").version("2.4.7").build()
        );
    }

    public static RunJobFlowResponse buildEMRCluster(EmrClient client, String name, String logFolder) {
        HadoopJarStepConfig debugStep = HadoopJarStepConfig
                .builder()
                .jar("command-runner.jar")
                .args("state-pusher-script")
                .build();

        StepConfig debug = StepConfig.builder()
                .name("Enable Debugging")
                .actionOnFailure(ActionOnFailure.TERMINATE_JOB_FLOW)
                .hadoopJarStep(debugStep)
                .build();

        RunJobFlowRequest request = RunJobFlowRequest.builder()
                .name(name)
                .releaseLabel("emr-5.32.1")
                .steps(debug)
                .applications(getStandardApplications())
                .logUri(logFolder)
                .instances(JobFlowInstancesConfig.builder()
                        .instanceCount(3)
                        .keepJobFlowAliveWhenNoSteps(true)
                        .masterInstanceType("m4.large")
                        .slaveInstanceType("m4.large")
                        .build())
                .build();

        return client.runJobFlow(request);
    }

    public static AddJobFlowStepsResponse submitJob(EmrClient client, String jobId, String jarFile, String className) {
        HadoopJarStepConfig sparkStepConfigJob = HadoopJarStepConfig.builder()
                .jar("command-runner.jar")
                .args("spark-submit", "--executor-memory", "1g", "--class", className, jarFile)
                .build();

        StepConfig sparkStep = StepConfig.builder()
                .name("Spark Step")
                .actionOnFailure(ActionOnFailure.CONTINUE)
                .hadoopJarStep(sparkStepConfigJob)
                .build();

        AddJobFlowStepsRequest request = AddJobFlowStepsRequest.builder()
                .jobFlowId(jobId)
                .steps(Arrays.asList(sparkStep))
                .build();

        return client.addJobFlowSteps(request);
    }

    @Test
    public void testJobFlow() {
        EmrClient client = TestUtils.getClientEMRV2();
        String jobId = buildEMRCluster(client, "test", "/tmp").jobFlowId();
        // TODO: upload JAR file to S3 - currently only submitting the job without checking the result
        submitJob(client, jobId, "s3://test.jar", "Test");
    }

}