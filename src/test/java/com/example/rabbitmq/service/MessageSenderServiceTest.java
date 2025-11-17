package com.example.rabbitmq.service;

import com.example.rabbitmq.entity.PendingMessageEntity;
import com.example.rabbitmq.model.DeliveryStatus;
import com.example.rabbitmq.model.Message;
import com.example.rabbitmq.model.MessageBatch;
import com.example.rabbitmq.repository.PendingMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageSenderServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ChannelMonitorService channelMonitorService;

    @Mock
    private PendingMessageRepository pendingMessageRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MessageSenderService messageSenderService;

    @BeforeEach
    void init() throws Exception {
        ReflectionTestUtils.setField(messageSenderService, "exchangeName", "test.exchange");
        ReflectionTestUtils.setField(messageSenderService, "deliveryDelaySeconds", 1L);
        ReflectionTestUtils.setField(messageSenderService, "maxDeliveryAttempts", 3);

        when(pendingMessageRepository.save(any())).thenAnswer(invocation -> {
            PendingMessageEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(ThreadLocalRandom.current().nextLong(1, 1000));
            }
            return entity;
        });

        lenient().when(objectMapper.writeValueAsString(any(Message.class))).thenReturn("{}");
        lenient().when(objectMapper.readValue(anyString(), eq(Message.class)))
                .thenReturn(new Message("payload", "type", "high"));
    }

    @Test
    void sendMessageBatchSchedulesMessages() {
        when(pendingMessageRepository.findByMessageId(anyString())).thenReturn(Optional.empty());

        MessageBatch batch = new MessageBatch(
                List.of(
                        new Message("m1", "test", "high"),
                        new Message("m2", "test", "low")
                ),
                "route.key"
        );

        MessageBatch result = messageSenderService.sendMessageBatch(batch).join();

        assertThat(result.getTotalCount()).isEqualTo(2);
        verify(pendingMessageRepository, times(2)).save(any());
    }

    @Test
    void sendSingleMessagePersistsPayload() {
        when(pendingMessageRepository.findByMessageId(anyString())).thenReturn(Optional.empty());

        Message message = new Message("body", "type", "medium");

        assertDoesNotThrow(() -> messageSenderService.sendSingleMessage(message, "route").join());
        verify(pendingMessageRepository).save(any());
    }

    @Test
    void attemptDeliveryMarksDeliveredOnSuccess() throws Exception {
        when(channelMonitorService.areChannelsAvailable()).thenReturn(true);

        PendingMessageEntity entity = baseEntity().build();

        ReflectionTestUtils.invokeMethod(messageSenderService, "attemptDelivery", entity);

        assertThat(entity.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        verify(rabbitTemplate).convertAndSend(eq("test.exchange"), eq("route.key"),
                any(Message.class), any(MessagePostProcessor.class));
        verify(pendingMessageRepository, atLeast(2)).save(any());
    }

    @Test
    void attemptDeliveryReschedulesOnFailure() throws Exception {
        when(channelMonitorService.areChannelsAvailable()).thenReturn(true);
        doThrow(new IllegalStateException("boom"))
                .when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), any(), any(MessagePostProcessor.class));

        PendingMessageEntity entity = baseEntity().build();

        ReflectionTestUtils.invokeMethod(messageSenderService, "attemptDelivery", entity);

        assertThat(entity.getStatus()).isEqualTo(DeliveryStatus.WAITING);
        assertThat(entity.getAttemptCount()).isEqualTo(1);
        verify(pendingMessageRepository, atLeast(2)).save(any());
    }

    @Test
    void attemptDeliveryStopsAfterMaxAttempts() throws Exception {
        ReflectionTestUtils.setField(messageSenderService, "maxDeliveryAttempts", 2);
        when(channelMonitorService.areChannelsAvailable()).thenReturn(true);
        doThrow(new IllegalStateException("boom"))
                .when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), any(), any(MessagePostProcessor.class));

        PendingMessageEntity entity = baseEntity()
                .attemptCount(1)
                .status(DeliveryStatus.WAITING)
                .build();

        ReflectionTestUtils.invokeMethod(messageSenderService, "attemptDelivery", entity);

        assertThat(entity.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        verify(pendingMessageRepository, atLeast(2)).save(any());
    }

    private PendingMessageEntity.PendingMessageEntityBuilder baseEntity() {
        LocalDateTime now = LocalDateTime.now().minusSeconds(5);
        return PendingMessageEntity.builder()
                .id(1L)
                .messageId("message-1")
                .payload("{}")
                .routingKey("route.key")
                .status(DeliveryStatus.WAITING)
                .attemptCount(0)
                .nextAttemptAt(LocalDateTime.now().minusSeconds(1))
                .createdAt(now)
                .updatedAt(now);
    }
}
