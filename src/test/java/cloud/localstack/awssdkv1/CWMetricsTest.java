package cloud.localstack.awssdkv1;

import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import cloud.localstack.LocalstackTestRunner;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    @Test
    public void testCWGetMetricData() {
        final AmazonCloudWatch cw = TestUtils.getClientCloudWatch();

        Metric metric = new Metric()
                .withNamespace("customNamespace")
                .withMetricName("MaxMemoryUsage")
                .withDimensions(
                        new Dimension().withName("id").withValue("specificId"),
                        new Dimension().withName("class").withValue("customNamespace"),
                        new Dimension().withName("level").withValue("INFO"),
                        new Dimension().withName("type").withValue("GAUGE"));

        /* List metric work */
        cw.listMetrics();

        String metricQueryId = "someName";
        MetricDataQuery metricDataQuery = new MetricDataQuery()
                .withId(metricQueryId)
                .withLabel("someLabel")
                .withReturnData(true)
                .withMetricStat(new MetricStat().withMetric(metric)
                        .withStat("Average")
                        .withUnit("None")
                        .withPeriod(5));

        GetMetricDataRequest getMetricDataRequest = new GetMetricDataRequest()
                .withMetricDataQueries(metricDataQuery)
                .withStartTime(new Date(System.currentTimeMillis() - 3600 * 1000))
                .withEndTime(new Date(System.currentTimeMillis()))
                .withMaxDatapoints(100);

        /* Get metricData work */
        GetMetricDataResult getMetricDataResult = cw.getMetricData(getMetricDataRequest);
        Assert.assertEquals(getMetricDataResult.getMetricDataResults().size(), 1);
        Assert.assertEquals(getMetricDataResult.getMetricDataResults().get(0).getId(), metricQueryId);
    }
}
