package com.example.rabbitmq.integration;

import com.example.rabbitmq.RabbitmqMessageSenderApplication;
import com.example.rabbitmq.model.Message;
import com.example.rabbitmq.service.MessageSenderService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = RabbitmqMessageSenderApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.rabbitmq.host=localhost",
        "spring.rabbitmq.port=5672",
        "spring.rabbitmq.username=guest",
        "spring.rabbitmq.password=guest",
        "rabbitmq.channel.max-available=5"
})
class RabbitMQIntegrationTest {

    @Autowired
    private MessageSenderService messageSenderService;

    @Test
    void testChannelAvailabilityCheck() {
        // Test that channel monitoring is working
        assertNotNull(messageSenderService.getChannelStatus());
    }

    @Test
    void testSendSingleMessage() {
        // Given
        Message message = new Message("Integration test message", "test", "high");

        // When & Then
        assertDoesNotThrow(() -> {
            CompletableFuture<Void> result = messageSenderService.sendSingleMessage(message, "test.key");
            result.join();
        });
    }

    @Test
    void testSendMultipleMessages() {
        // Given
        List<Message> messages = Arrays.asList(
                new Message("Message 1", "test", "high"),
                new Message("Message 2", "test", "medium"),
                new Message("Message 3", "test", "low")
        );

        // When & Then
        assertDoesNotThrow(() -> {
            CompletableFuture<List<Message>> result = messageSenderService.sendMessagesAsync(messages, "test.key");
            List<Message> sentMessages = result.join();
            assertEquals(3, sentMessages.size());
        });
    }
}