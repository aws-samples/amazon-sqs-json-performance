package example;

import com.amazonaws.services.sqs.AmazonSQS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class AmazonSqsClientFactoryTest {

    @Test
    void testReturnsSqsClient() {
        AmazonSQS client = AmazonSqsClientFactory.sqsClient();
        Assertions.assertInstanceOf(AmazonSQS.class, client);
    }

    @Test
    void testReturnsJsonSqsClient() {
        com.amazonaws.services.sqs.json.AmazonSQS client = AmazonSqsClientFactory.jsonSqsClient();
        Assertions.assertInstanceOf(com.amazonaws.services.sqs.json.AmazonSQS.class, client);
    }
}