package com.example.rabbitmq.service;

import com.example.rabbitmq.model.Message;
import com.example.rabbitmq.model.MessageBatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageSenderServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ChannelMonitorService channelMonitorService;

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
        // Given
        when(channelMonitorService.areChannelsAvailable()).thenReturn(true);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(), any(), any());

        // When
        CompletableFuture<MessageBatch> result = messageSenderService.sendMessageBatch(testBatch);

        // Then
        assertNotNull(result);
        MessageBatch batchResult = result.join();
        assertEquals(testBatch.getBatchId(), batchResult.getBatchId());
        assertEquals(3, batchResult.getTotalCount());
        verify(channelMonitorService, atLeastOnce()).areChannelsAvailable();
    }

    @Test
    void testSendMessageBatch_NoChannelsAvailable() {
        // Given
        when(channelMonitorService.areChannelsAvailable()).thenReturn(false);

        // When & Then
        CompletableFuture<MessageBatch> result = messageSenderService.sendMessageBatch(testBatch);
        assertThrows(RuntimeException.class, () -> result.join());
    }

    @Test
    void testSendSingleMessage_Success() {
        // Given
        Message message = new Message("Test message", "test", "high");
        when(channelMonitorService.areChannelsAvailable()).thenReturn(true);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(), any(), any());

        // When
        CompletableFuture<Void> result = messageSenderService.sendSingleMessage(message, "test.key");

        // Then
        assertNotNull(result);
        assertDoesNotThrow(() -> result.join());
        verify(rabbitTemplate).convertAndSend(eq("message.exchange"), eq("test.key"), eq(message), any(), isNull());
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
        // Given
        when(channelMonitorService.areChannelsAvailable()).thenReturn(true);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(), any(), any());

        // When
        CompletableFuture<List<Message>> result = messageSenderService.sendMessagesAsync(testMessages, "test.key");

        // Then
        assertNotNull(result);
        List<Message> messages = result.join();
        assertEquals(3, messages.size());
        verify(channelMonitorService, atLeastOnce()).areChannelsAvailable();
    }
}