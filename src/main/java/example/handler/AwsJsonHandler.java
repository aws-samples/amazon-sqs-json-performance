package example.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import example.JsonSqsFacade;

public class AwsJsonHandler implements RequestHandler<SQSEvent, Integer> {

    protected static final JsonSqsFacade jsonSqs = new JsonSqsFacade();

    @Override
    public Integer handleRequest(SQSEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        String message = event.getRecords().get(0).getBody();
        Operation operation = Operation.valueOf(message);
        int count = 0;
        if (operation == Operation.SEND) {
            count = jsonSqs.sendMessages(logger);
            logger.log("Sent totally " + count + " messages using AwsJson!");
        } else if(operation == Operation.PROCESS) {
            logger.log("Processing using AwsJson SQS client");
            count = jsonSqs.receiveAndDeleteMessages(logger);
            logger.log("Processed totally " + count + " messages using AwsJson!");
        }
        return count;
    }
}
