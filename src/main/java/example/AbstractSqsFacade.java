package example;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.apache.commons.lang3.RandomStringUtils;

public abstract class AbstractSqsFacade {
    protected static final String QUEUE_URL = String.format("https://sqs.us-east-1.amazonaws.com/%s/PerfTestQueue", System.getenv("AWS_ACCOUNT_ID"));
    public static final Integer POLL_PERIOD = 3000;
    public static final Integer MESSAGES_TO_SEND = 250;
    public static final Integer MESSAGES_TO_RECV = 100;
    public static final Integer MESSAGE_SIZE = 4000;
    protected final CloudwatchFacade cwFacade;

    AbstractSqsFacade() {
        this.cwFacade = new CloudwatchFacade();
    }

    AbstractSqsFacade(CloudwatchFacade cwFacade) {
        this.cwFacade = cwFacade;
    }

    protected interface MessageSender {
        Object send(String message);
    }

    public interface MessageProcessor {
        Integer process();
    }

    public abstract Integer sendMessages(LambdaLogger logger);

    public abstract Integer receiveAndDeleteMessages(LambdaLogger logger);

    protected Integer send(LambdaLogger logger, MessageSender sender, String protocol) {
        String message = RandomStringUtils.randomAlphanumeric(MESSAGE_SIZE);
        int numberOfMessagesSent = 0;
        while (numberOfMessagesSent < MESSAGES_TO_SEND) {
            if (numberOfMessagesSent < 5) {
                long start = System.currentTimeMillis();
                String requestId = (String) sender.send(message);
                Long duration = System.currentTimeMillis() - start;
                cwFacade.putSendDuration(duration, protocol);
                logger.log("Request " + requestId + " took " + duration + " ms");
            } else {
                sender.send(message);
            }
            numberOfMessagesSent++;
        }
        logger.log("Sent " + numberOfMessagesSent + " messages");
        return numberOfMessagesSent;
    }

    protected Integer process(LambdaLogger logger, MessageProcessor processor, String protocol) {
        long startTime = now();
        int processed = 0;
        int toProcess = MESSAGES_TO_RECV;
        while (toProcess > 0 && withinPollPeriod(startTime)) {
            long start = System.currentTimeMillis();
            int p = processor.process();
            cwFacade.putProcessDuration(System.currentTimeMillis() - start, protocol);
            processed += p;
            toProcess -= p;
        }
        logger.log("Processed " + processed + " messages");
        return processed;
    }

    private boolean withinPollPeriod(long startTime) {
        return now() - startTime < POLL_PERIOD;
    }

    private long now() {
        return System.currentTimeMillis();
    }
}
