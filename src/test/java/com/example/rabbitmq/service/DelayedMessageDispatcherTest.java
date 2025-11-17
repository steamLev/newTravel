package com.example.rabbitmq.service;

import com.example.rabbitmq.model.DeliveryStatus;
import com.example.rabbitmq.model.OutboundMessageEntity;
import com.example.rabbitmq.repository.OutboundMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.DelayQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DelayedMessageDispatcherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private OutboundMessageRepository outboundMessageRepository;

    @Mock
    private ChannelMonitorService channelMonitorService;

    @InjectMocks
    private DelayedMessageDispatcher dispatcher;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(dispatcher, "exchangeName", "message.exchange");
        ReflectionTestUtils.setField(dispatcher, "deliveryDelaySeconds", 1L);
        ReflectionTestUtils.setField(dispatcher, "configuredMaxAttempts", 5);

        when(outboundMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void processMessage_sendsSuccessfully() {
        OutboundMessageEntity entity = baseEntity();
        when(outboundMessageRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(channelMonitorService.areChannelsAvailable()).thenReturn(true);

        dispatcher.processMessage(entity.getId());

        assertEquals(DeliveryStatus.SENT, entity.getStatus());
        assertEquals(1, entity.getAttemptCount());
        assertNotNull(entity.getDeliveredAt());
        verify(rabbitTemplate).convertAndSend(
                eq("message.exchange"),
                eq("routing.key"),
                any(),
                org.mockito.ArgumentMatchers.<org.springframework.amqp.core.MessagePostProcessor>any());
        DelayQueue<?> queue = (DelayQueue<?>) ReflectionTestUtils.getField(dispatcher, "delayQueue");
        assertEquals(0, queue.size());
    }

    @Test
    void processMessage_reschedulesOnFailure() {
        OutboundMessageEntity entity = baseEntity();
        when(outboundMessageRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(channelMonitorService.areChannelsAvailable()).thenReturn(true);
        doThrow(new RuntimeException("Rabbit down"))
                .when(rabbitTemplate)
                .convertAndSend(
                        eq("message.exchange"),
                        eq("routing.key"),
                        any(),
                        org.mockito.ArgumentMatchers.<org.springframework.amqp.core.MessagePostProcessor>any());

        dispatcher.processMessage(entity.getId());

        assertEquals(DeliveryStatus.PENDING, entity.getStatus());
        assertEquals(1, entity.getAttemptCount());
        assertNotNull(entity.getNextAttemptAt());
        DelayQueue<?> queue = (DelayQueue<?>) ReflectionTestUtils.getField(dispatcher, "delayQueue");
        assertEquals(1, queue.size());
    }

    @Test
    void processMessage_marksFailedAfterMaxAttempts() {
        OutboundMessageEntity entity = baseEntity();
        entity.setMaxAttempts(1);
        when(outboundMessageRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(channelMonitorService.areChannelsAvailable()).thenReturn(true);
        doThrow(new RuntimeException("Rabbit down"))
                .when(rabbitTemplate)
                .convertAndSend(
                        eq("message.exchange"),
                        eq("routing.key"),
                        any(),
                        org.mockito.ArgumentMatchers.<org.springframework.amqp.core.MessagePostProcessor>any());

        dispatcher.processMessage(entity.getId());

        assertEquals(DeliveryStatus.FAILED, entity.getStatus());
        DelayQueue<?> queue = (DelayQueue<?>) ReflectionTestUtils.getField(dispatcher, "delayQueue");
        assertEquals(0, queue.size());
    }

    private OutboundMessageEntity baseEntity() {
        return OutboundMessageEntity.builder()
                .id(UUID.randomUUID().toString())
                .content("payload")
                .type("test")
                .priority("high")
                .routingKey("routing.key")
                .status(DeliveryStatus.PENDING)
                .attemptCount(0)
                .maxAttempts(5)
                .nextAttemptAt(Instant.now())
                .build();
    }
}
