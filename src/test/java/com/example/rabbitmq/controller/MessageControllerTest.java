package com.example.rabbitmq.controller;

import com.example.rabbitmq.model.ChannelStatus;
import com.example.rabbitmq.model.Message;
import com.example.rabbitmq.service.ChannelMonitorService;
import com.example.rabbitmq.service.MessageSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private MessageSenderService messageSenderService;

    @Mock
    private ChannelMonitorService channelMonitorService;

    @InjectMocks
    private MessageController messageController;

    private List<Message> testMessages;
    private ChannelStatus testChannelStatus;

    @BeforeEach
    void setUp() {
        testMessages = Arrays.asList(
                new Message("Test message 1", "test", "high"),
                new Message("Test message 2", "test", "medium")
        );
        testChannelStatus = new ChannelStatus(true, 5, 10);
    }

    @Test
    void testSendMessages_Success() {
        // Given
        when(messageSenderService.canSendMessages()).thenReturn(true);
        when(messageSenderService.sendMessageBatch(any())).thenReturn(CompletableFuture.completedFuture(null));

        // When
        CompletableFuture<ResponseEntity<String>> result = messageController.sendMessages(testMessages, "test.key");

        // Then
        assertNotNull(result);
        ResponseEntity<String> response = result.join();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Messages sent successfully"));
    }

    @Test
    void testSendMessages_NoChannelsAvailable() {
        // Given
        when(messageSenderService.canSendMessages()).thenReturn(false);

        // When
        CompletableFuture<ResponseEntity<String>> result = messageController.sendMessages(testMessages, "test.key");

        // Then
        assertNotNull(result);
        ResponseEntity<String> response = result.join();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("No available channels"));
    }

    @Test
    void testGetChannelStatus() {
        // Given
        when(channelMonitorService.getChannelStatus()).thenReturn(testChannelStatus);

        // When
        ResponseEntity<ChannelStatus> result = messageController.getChannelStatus();

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(testChannelStatus, result.getBody());
        verify(channelMonitorService).getChannelStatus();
    }

    @Test
    void testAreChannelsAvailable() {
        // Given
        when(messageSenderService.canSendMessages()).thenReturn(true);

        // When
        ResponseEntity<Boolean> result = messageController.areChannelsAvailable();

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody());
        verify(messageSenderService).canSendMessages();
    }

    @Test
    void testCreateTestMessages() {
        // When
        ResponseEntity<List<Message>> result = messageController.createTestMessages(3, "test");

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(3, result.getBody().size());
        assertEquals("test", result.getBody().get(0).getType());
    }

    @Test
    void testSendTestMessages_Success() {
        // Given
        when(messageSenderService.canSendMessages()).thenReturn(true);
        when(messageSenderService.sendMessageBatch(any())).thenReturn(CompletableFuture.completedFuture(null));

        // When
        CompletableFuture<ResponseEntity<String>> result = messageController.sendTestMessages(3, "test", "test.key");

        // Then
        assertNotNull(result);
        ResponseEntity<String> response = result.join();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Test messages sent successfully"));
    }
}