package example.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import example.SqsFacade;

public class AwsQueryHandler implements RequestHandler<SQSEvent, Integer> {

    protected static final SqsFacade sqs = new SqsFacade();

    @Override
    public Integer handleRequest(SQSEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        String message = event.getRecords().get(0).getBody();
        Operation operation = Operation.valueOf(message);
        int count = 0;
        if (operation == Operation.SEND) {
            count = sqs.sendMessages(logger);
            logger.log("Sent totally " + count + " messages using AwsQuery!");
        } else if(operation == Operation.PROCESS) {
            logger.log("Processing using AwsQuery SQS client");
            count = sqs.receiveAndDeleteMessages(logger);
            logger.log("Processed totally " + count + " messages using AwsQuery!");
        }
        return count;
    }
}
