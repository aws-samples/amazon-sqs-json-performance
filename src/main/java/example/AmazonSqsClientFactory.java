package example;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.json.AmazonSQSClientBuilder;

public class AmazonSqsClientFactory {

    private static final String DEFAULT_REGION = "us-east-1";

    public static AmazonSQS sqsClient() {
        com.amazonaws.services.sqs.AmazonSQSClientBuilder builder = AmazonSQSClient.builder();
        if (builder.getRegion() == null) {
            builder.setRegion(DEFAULT_REGION);
        }
        return builder.build();
    }

    public static com.amazonaws.services.sqs.json.AmazonSQS jsonSqsClient() {
        AmazonSQSClientBuilder builder = com.amazonaws.services.sqs.json.AmazonSQSClient.builder();
        if (builder.getRegion() == null) {
            builder.setRegion(DEFAULT_REGION);
        }
        return builder.build();
    }
}
