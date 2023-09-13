package example;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.util.List;
import java.util.stream.Collectors;

public class SqsFacade extends AbstractSqsFacade {
    private static final String PROTOCOL = "QUERY";
    private final AmazonSQS sqs;

    public SqsFacade(AmazonSQS sqs, CloudwatchFacade cwFacade) {
        super(cwFacade);
        this.sqs = sqs;
    }

    public SqsFacade() {
        this.sqs = AmazonSqsClientFactory.sqsClient();
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
