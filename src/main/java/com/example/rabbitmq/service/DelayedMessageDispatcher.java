package com.example.rabbitmq.service;

import com.example.rabbitmq.model.DeliveryStatus;
import com.example.rabbitmq.model.Message;
import com.example.rabbitmq.model.MessageBatch;
import com.example.rabbitmq.model.OutboundMessageEntity;
import com.example.rabbitmq.repository.OutboundMessageRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DelayedMessageDispatcher {

    private final RabbitTemplate rabbitTemplate;
    private final OutboundMessageRepository messageRepository;
    private final ChannelMonitorService channelMonitorService;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${message.delivery.delay-seconds:60}")
    private long deliveryDelaySeconds;

    @Value("${message.delivery.max-attempts:5}")
    private int configuredMaxAttempts;

    private final DelayQueue<DelayedMessageTask> delayQueue = new DelayQueue<>();
    private final ExecutorService dispatcherExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "delayed-message-dispatcher");
        thread.setDaemon(true);
        return thread;
    });

    @PostConstruct
    public void start() {
        restoreActiveMessages();
        dispatcherExecutor.submit(this::processLoop);
    }

    @PreDestroy
    public void shutdown() {
        dispatcherExecutor.shutdownNow();
    }

    public void enqueueBatch(MessageBatch batch) {
        String routingKey = resolveRoutingKey(batch.getRoutingKey());
        batch.getMessages().forEach(message -> enqueueSingle(message, routingKey));
    }

    public void enqueueSingle(Message message, String routingKey) {
        String resolvedKey = resolveRoutingKey(routingKey);
        Message normalized = normalizeMessage(message);
        OutboundMessageEntity entity = OutboundMessageEntity.builder()
                .id(normalized.getId())
                .content(normalized.getContent())
                .type(normalized.getType())
                .priority(normalized.getPriority())
                .routingKey(resolvedKey)
                .status(DeliveryStatus.PENDING)
                .attemptCount(0)
                .maxAttempts(configuredMaxAttempts)
                .nextAttemptAt(Instant.now().plusSeconds(deliveryDelaySeconds))
                .build();

        messageRepository.save(entity);
        delayQueue.offer(new DelayedMessageTask(entity.getId(), entity.getNextAttemptAt()));
        log.info("Сообщение {} добавлено в очередь на отправку через {} сек", entity.getId(), deliveryDelaySeconds);
    }

    private void restoreActiveMessages() {
        List<OutboundMessageEntity> activeMessages =
                messageRepository.findActiveMessages(EnumSet.of(DeliveryStatus.PENDING, DeliveryStatus.IN_PROGRESS));
        Instant now = Instant.now();

        for (OutboundMessageEntity entity : activeMessages) {
            Instant nextAttempt = entity.getNextAttemptAt();
            if (nextAttempt == null || nextAttempt.isBefore(now)) {
                nextAttempt = now.plusSeconds(1);
                entity.setNextAttemptAt(nextAttempt);
                entity.setStatus(DeliveryStatus.PENDING);
                messageRepository.save(entity);
            }
            delayQueue.offer(new DelayedMessageTask(entity.getId(), nextAttempt));
        }

        log.info("Восстановлено {} сообщений для повторной отправки", activeMessages.size());
    }

    private void processLoop() {
        log.info("Старт обработчика отложенных сообщений");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DelayedMessageTask task = delayQueue.take();
                processMessage(task.messageId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Ошибка во время обработки очереди отложенных сообщений", e);
            }
        }
        log.info("Обработчик отложенных сообщений остановлен");
    }

    protected void processMessage(String messageId) {
        messageRepository.findById(messageId).ifPresent(entity -> {
            if (entity.getStatus().isTerminal()) {
                return;
            }

            if (entity.getAttemptCount() >= entity.getMaxAttempts()) {
                entity.setStatus(DeliveryStatus.FAILED);
                entity.setLastError("Превышено количество попыток");
                messageRepository.save(entity);
                return;
            }

            if (!channelMonitorService.areChannelsAvailable()) {
                reschedule(entity, "Каналы недоступны");
                return;
            }

            try {
                entity.setStatus(DeliveryStatus.IN_PROGRESS);
                entity.setLastAttemptAt(Instant.now());
                entity.setAttemptCount(entity.getAttemptCount() + 1);
                messageRepository.save(entity);

                publish(entity);

                entity.setStatus(DeliveryStatus.SENT);
                entity.setDeliveredAt(Instant.now());
                entity.setLastError(null);
                messageRepository.save(entity);

                log.info("Сообщение {} успешно доставлено", entity.getId());
            } catch (Exception ex) {
                log.error("Ошибка отправки сообщения {}", entity.getId(), ex);
                reschedule(entity, ex.getMessage());
            }
        });
    }

    private void reschedule(OutboundMessageEntity entity, String errorMessage) {
        entity.setStatus(entity.getAttemptCount() >= entity.getMaxAttempts() ? DeliveryStatus.FAILED : DeliveryStatus.PENDING);
        entity.setLastError(errorMessage);

        if (!entity.getStatus().isTerminal()) {
            Instant nextAttempt = Instant.now().plusSeconds(deliveryDelaySeconds);
            entity.setNextAttemptAt(nextAttempt);
            messageRepository.save(entity);
            delayQueue.offer(new DelayedMessageTask(entity.getId(), nextAttempt));
            log.warn("Сообщение {} будет отправлено повторно. Попытка {}/{}", entity.getId(),
                    entity.getAttemptCount(), entity.getMaxAttempts());
        } else {
            messageRepository.save(entity);
            log.error("Сообщение {} помечено как FAILED после {} попыток", entity.getId(), entity.getAttemptCount());
        }
    }

    private void publish(OutboundMessageEntity entity) {
        Message domainMessage = new Message();
        domainMessage.setId(entity.getId());
        domainMessage.setContent(entity.getContent());
        domainMessage.setType(entity.getType());
        domainMessage.setPriority(entity.getPriority());
        domainMessage.setTimestamp(LocalDateTime.now());

        rabbitTemplate.convertAndSend(
                exchangeName,
                entity.getRoutingKey(),
                domainMessage,
                msg -> {
                    MessageProperties properties = msg.getMessageProperties();
                    properties.setMessageId(entity.getId());
                    properties.setPriority(getPriorityValue(entity.getPriority()));
                    return msg;
                }
        );
    }

    private Message normalizeMessage(Message message) {
        if (message.getId() == null) {
            message.setId(UUID.randomUUID().toString());
        }
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        return message;
    }

    private int getPriorityValue(String priority) {
        if (priority == null) {
            return 0;
        }
        return switch (priority.toLowerCase()) {
            case "high" -> 10;
            case "medium" -> 5;
            case "low" -> 1;
            default -> 0;
        };
    }

    private String resolveRoutingKey(String routingKey) {
        return routingKey != null ? routingKey : "message.routing.key";
    }

    private record DelayedMessageTask(String messageId, Instant executeAt) implements Delayed {

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = executeAt.toEpochMilli() - Instant.now().toEpochMilli();
            return unit.convert(Math.max(diff, 0), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            if (other == this) {
                return 0;
            }
            long diff = getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
            return Long.compare(diff, 0);
        }
    }
}
