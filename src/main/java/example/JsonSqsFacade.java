package example;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sqs.json.AmazonSQS;
import com.amazonaws.services.sqs.json.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.json.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.json.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.json.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.json.model.SendMessageRequest;

import java.util.List;
import java.util.stream.Collectors;

public class JsonSqsFacade extends AbstractSqsFacade {
    private static final String PROTOCOL = "JSON";
    private final AmazonSQS sqs;

    public JsonSqsFacade() {
        this.sqs = AmazonSqsClientFactory.jsonSqsClient();
    }

    public JsonSqsFacade(AmazonSQS sqs, CloudwatchFacade cwFacade) {
        super(cwFacade);
        this.sqs = sqs;
    }

    @Override
    public Integer sendMessages(LambdaLogger logger) {
        return send(logger, (message ->
                sqs.sendMessage(new SendMessageRequest(QUEUE_URL, message)).getSdkResponseMetadata().getRequestId()), PROTOCOL);
    }

    @Override
    public Integer receiveAndDeleteMessages(LambdaLogger logger) {
        return process(logger, () -> {
            ReceiveMessageResult result = sqs.receiveMessage(
                    new ReceiveMessageRequest().withWaitTimeSeconds(2).withQueueUrl(QUEUE_URL).withMaxNumberOfMessages(10)
            );
            List<DeleteMessageBatchRequestEntry> entries = result.getMessages().stream().map(it ->
                    new DeleteMessageBatchRequestEntry(String.valueOf(result.getMessages().indexOf(it)), it.getReceiptHandle())
            ).collect(Collectors.toList());
            if (!entries.isEmpty()) {
                sqs.deleteMessageBatch(new DeleteMessageBatchRequest().withQueueUrl(QUEUE_URL).withEntries(entries));
            }
            return entries.size();
        }, PROTOCOL);
    }
}
