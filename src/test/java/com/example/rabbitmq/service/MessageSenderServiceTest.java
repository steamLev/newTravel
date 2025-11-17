package com.example.rabbitmq.service;

import com.example.rabbitmq.model.Message;
import com.example.rabbitmq.model.MessageBatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageSenderServiceTest {

    @Mock
    private ChannelMonitorService channelMonitorService;

    @Mock
    private DelayedMessageDispatcher delayedMessageDispatcher;

    @InjectMocks
    private MessageSenderService messageSenderService;

    private List<Message> testMessages;
    private MessageBatch testBatch;

    @BeforeEach
    void setUp() {
        testMessages = Arrays.asList(
                new Message("Test message 1", "test", "high"),
                new Message("Test message 2", "test", "medium"),
                new Message("Test message 3", "test", "low")
        );
        testBatch = new MessageBatch(testMessages, "test.routing.key");
    }

    @Test
    void testSendMessageBatch_Success() {
        // When
        CompletableFuture<MessageBatch> result = messageSenderService.sendMessageBatch(testBatch);

        // Then
        assertNotNull(result);
        MessageBatch batchResult = result.join();
        assertEquals(testBatch.getBatchId(), batchResult.getBatchId());
        assertEquals(3, batchResult.getTotalCount());
        verify(delayedMessageDispatcher).enqueueBatch(testBatch);
    }

    @Test
    void testSendSingleMessage_Success() {
        // Given
        Message message = new Message("Test message", "test", "high");

        // When
        CompletableFuture<Void> result = messageSenderService.sendSingleMessage(message, "test.key");

        // Then
        assertNotNull(result);
        assertDoesNotThrow(() -> result.join());
        verify(delayedMessageDispatcher).enqueueSingle(message, "test.key");
    }

    @Test
    void testCanSendMessages() {
        // Given
        when(channelMonitorService.areChannelsAvailable()).thenReturn(true);

        // When
        boolean result = messageSenderService.canSendMessages();

        // Then
        assertTrue(result);
        verify(channelMonitorService).areChannelsAvailable();
    }

    @Test
    void testSendMessagesAsync_Success() {
        // When
        CompletableFuture<List<Message>> result = messageSenderService.sendMessagesAsync(testMessages, "test.key");

        // Then
        assertNotNull(result);
        List<Message> messages = result.join();
        assertEquals(3, messages.size());
        verify(delayedMessageDispatcher, times(testMessages.size()))
                .enqueueSingle(any(Message.class), eq("test.key"));
    }
}