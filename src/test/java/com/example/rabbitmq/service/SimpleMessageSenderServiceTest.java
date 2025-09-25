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
class SimpleMessageSenderServiceTest {

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
    void testCanSendMessages_NoChannelsAvailable() {
        // Given
        when(channelMonitorService.areChannelsAvailable()).thenReturn(false);

        // When
        boolean result = messageSenderService.canSendMessages();

        // Then
        assertFalse(result);
        verify(channelMonitorService).areChannelsAvailable();
    }

    @Test
    void testGetChannelStatus() {
        // Given
        when(channelMonitorService.getChannelStatus()).thenReturn(
                new com.example.rabbitmq.model.ChannelStatus(true, 5, 10)
        );

        // When
        String status = messageSenderService.getChannelStatus();

        // Then
        assertNotNull(status);
        assertTrue(status.contains("AVAILABLE"));
        verify(channelMonitorService).getChannelStatus();
    }
}