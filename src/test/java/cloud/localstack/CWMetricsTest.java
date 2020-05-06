package cloud.localstack;

import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Test integration of CloudWatch metrics with LocalStack
 * Issue: https://github.com/localstack/localstack/issues/712
 */
@RunWith(LocalstackTestRunner.class)
@LocalstackDockerProperties(ignoreDockerRunErrors=true)
public class CWMetricsTest {
    @Test
    public void testCWMetricsAPIs() throws ParseException {
        final AmazonCloudWatch cw = TestUtils.getClientCloudWatch();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Dimension dimension = new Dimension()
                .withName("UNIQUE_PAGES")
                .withValue("URLS");

        /* Put metric data without value */
        MetricDatum datum = new MetricDatum()
                .withMetricName("PAGES_VISITED")
                .withUnit(StandardUnit.None)
                .withTimestamp(dateFormat.parse("2019-01-02"))
                .withDimensions(dimension);

        PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest()
                .withNamespace("SITE/TRAFFIC")
                .withMetricData(datum);

        cw.putMetricData(putMetricDataRequest);

        /* Get metric statistics */
        GetMetricStatisticsRequest getMetricStatisticsRequest = new GetMetricStatisticsRequest()
                .withMetricName("PAGES_VISITED")
                .withNamespace("SITE/TRAFFIC")
                /* When calling GetMetricStatistics, must specify either Statistics or ExtendedStatistics, but not both.
                   https://docs.aws.amazon.com/cli/latest/reference/cloudwatch/get-metric-statistics.html */
                .withStatistics("Statistics")
                .withStartTime(dateFormat.parse("2019-01-01"))
                .withEndTime(dateFormat.parse("2019-01-03"))
                .withPeriod(360);

        GetMetricStatisticsResult metricStatistics = cw.getMetricStatistics(getMetricStatisticsRequest);
        Assert.assertEquals(metricStatistics.getDatapoints().size(), 1);

        /* List metric work as expectation */
        ListMetricsResult metrics = cw.listMetrics(new ListMetricsRequest());
        Assert.assertEquals(metrics.getMetrics().size(), 1);
    }
}
