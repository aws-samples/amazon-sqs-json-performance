package example;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

public class CloudwatchFacade {
    private final AmazonCloudWatch cloudWatch;

    public CloudwatchFacade() {
        this.cloudWatch = AmazonCloudWatchClientBuilder.defaultClient();
    }

    public void putSendDuration(Long duration, String protocol) {
        put(duration, protocol, "SEND_DURATION");
    }

    public void putProcessDuration(Long duration, String protocol) {
        put(duration, protocol, "PROCESS_DURATION");
    }

    private void put(Long duration, String protocol, String processDuration) {
        Dimension dimension = new Dimension()
                .withName("WIRE_PROTOCOL")
                .withValue(protocol);
        MetricDatum datum = new MetricDatum()
                .withMetricName(processDuration)
                .withUnit(StandardUnit.Milliseconds)
                .withValue(duration.doubleValue())
                .withDimensions(dimension);
        PutMetricDataRequest request = new PutMetricDataRequest()
                .withNamespace("JSON/EXPERIMENT")
                .withMetricData(datum);
        cloudWatch.putMetricData(request);
    }
}
