package com.example.rabbitmq.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleMessageSenderServiceTest {

    @Mock
    private DelayedMessageDispatcher delayedMessageDispatcher;

    @Mock
    private ChannelMonitorService channelMonitorService;

    @InjectMocks
    private MessageSenderService messageSenderService;

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