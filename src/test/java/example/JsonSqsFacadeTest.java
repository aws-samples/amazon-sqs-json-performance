package example;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sqs.json.AmazonSQS;
import com.amazonaws.services.sqs.json.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.json.model.Message;
import com.amazonaws.services.sqs.json.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.json.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.json.model.SendMessageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class JsonSqsFacadeTest {

    @Mock
    private AmazonSQS mockSqs;

    @Mock
    private LambdaLogger logger;

    @Mock
    private ReceiveMessageResult mockResult;

    @Mock
    private SendMessageResult mockSendResult;

    @Mock
    private CloudwatchFacade cwFacade;

    private JsonSqsFacade sqsFacade;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doNothing().when(logger).log(anyString());
        List<Message> messages = new ArrayList<>();
        messages.add(new Message().withReceiptHandle("handle"));
        when(mockResult.getMessages()).thenReturn(messages);
        when(mockSendResult.getSdkResponseMetadata()).thenReturn(mock());
        when(mockSqs.sendMessage(any())).thenReturn(mockSendResult);
        when(mockSqs.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(mockResult);
        doNothing().when(cwFacade).putSendDuration(any(), anyString());
        sqsFacade = new JsonSqsFacade(mockSqs, cwFacade);
    }

    @Test
    void testSend() {
        final AtomicInteger counter = new AtomicInteger();
        Integer totalSent = sqsFacade.send(logger, (any) -> {
            counter.incrementAndGet();
            return "som-req-id";
        }, "JSON");
        assertEquals(250, totalSent);
        assertEquals(250, counter.get());
    }

    @Test
    void testSendMessage() {
        Integer totalSent = sqsFacade.sendMessages(logger);
        verify(mockSqs, times(250)).sendMessage(any());
        assertEquals(250, totalSent);
        verifyNoMoreInteractions(mockSqs);
    }

    @Test
    void testReceive() {
        Integer totalProcessed = sqsFacade.process(logger, () -> 1, "JSON");
        assertEquals(100, totalProcessed);
    }

    @Test
    void testReceiveAndDeleteMessages() {
        Integer totalProcessed = sqsFacade.receiveAndDeleteMessages(logger);
        verify(mockSqs, times(100)).receiveMessage(any(ReceiveMessageRequest.class));
        verify(mockSqs, times(100)).deleteMessageBatch(any(DeleteMessageBatchRequest.class));
        assertEquals(100, totalProcessed);
        verifyNoMoreInteractions(mockSqs);
    }
}