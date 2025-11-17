package com.example.rabbitmq.service;

import com.example.rabbitmq.entity.PendingMessageEntity;
import com.example.rabbitmq.model.DeliveryStatus;
import com.example.rabbitmq.model.Message;
import com.example.rabbitmq.model.MessageBatch;
import com.example.rabbitmq.repository.PendingMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSenderService {

    private final RabbitTemplate rabbitTemplate;
    private final ChannelMonitorService channelMonitorService;
    private final PendingMessageRepository pendingMessageRepository;
    private final ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${message.delivery.delay-seconds:60}")
    private long deliveryDelaySeconds;

    @Value("${message.delivery.max-attempts:5}")
    private int maxDeliveryAttempts;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final ExecutorService dispatcherExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "delayed-delivery-dispatcher");
        thread.setDaemon(true);
        return thread;
    });

    private final DelayQueue<DelayedDeliveryTask> deliveryQueue = new DelayQueue<>();
    private final AtomicBoolean dispatcherRunning = new AtomicBoolean(false);
    private final ZoneId zoneId = ZoneId.systemDefault();

    @PostConstruct
    public void initializeDispatcher() {
        dispatcherRunning.set(true);
        restorePendingMessages();
        dispatcherExecutor.submit(this::processQueue);
    }

    @PreDestroy
    public void shutdownExecutors() {
        dispatcherRunning.set(false);
        dispatcherExecutor.shutdownNow();
        executorService.shutdown();
    }

    /**
     * Планирует отправку списка сообщений с задержкой.
     */
    public CompletableFuture<MessageBatch> sendMessageBatch(MessageBatch messageBatch) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Scheduling batch {} with {} messages for deferred delivery",
                    messageBatch.getBatchId(), messageBatch.getTotalCount());
            try {
                messageBatch.getMessages().forEach(message ->
                        scheduleMessageForDelivery(message, messageBatch.getRoutingKey()));
                return messageBatch;
            } catch (Exception e) {
                log.error("Failed to schedule batch {}", messageBatch.getBatchId(), e);
                throw new RuntimeException("Failed to schedule message batch", e);
            }
        }, executorService);
    }

    /**
     * Планирует отправку одного сообщения.
     */
    public CompletableFuture<Void> sendSingleMessage(Message message, String routingKey) {
        return CompletableFuture.runAsync(() ->
                scheduleMessageForDelivery(message, routingKey), executorService);
    }

    /**
     * Планирует отправку произвольного списка сообщений.
     */
    public CompletableFuture<List<Message>> sendMessagesAsync(List<Message> messages, String routingKey) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Scheduling {} messages for deferred delivery", messages.size());
            messages.forEach(message -> scheduleMessageForDelivery(message, routingKey));
            return messages;
        }, executorService);
    }

    /**
     * Проверяет текущий статус каналов (используется для диагностики).
     */
    public boolean canSendMessages() {
        return channelMonitorService.areChannelsAvailable();
    }

    /**
     * Возвращает диагностический статус каналов RabbitMQ.
     */
    public String getChannelStatus() {
        return channelMonitorService.getChannelStatus().toString();
    }

    private void processQueue() {
        while (dispatcherRunning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                DelayedDeliveryTask task = deliveryQueue.take();
                handleTask(task.getEntityId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Unexpected error in deferred delivery dispatcher", e);
            }
        }
    }

    private void handleTask(Long entityId) {
        pendingMessageRepository.findById(entityId).ifPresent(entity -> {
            if (entity.getStatus() == DeliveryStatus.DELIVERED || entity.getStatus() == DeliveryStatus.FAILED) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            if (entity.getNextAttemptAt().isAfter(now)) {
                enqueueForDelivery(entity);
                return;
            }

            attemptDelivery(entity);
        });
    }

    private void attemptDelivery(PendingMessageEntity entity) {
        if (!channelMonitorService.areChannelsAvailable()) {
            log.debug("Channels unavailable, rescheduling message {}", entity.getMessageId());
            handleFailure(entity, new IllegalStateException("Channels unavailable"));
            return;
        }

        try {
            entity.setStatus(DeliveryStatus.IN_PROGRESS);
            entity.setUpdatedAt(LocalDateTime.now());
            pendingMessageRepository.save(entity);

            Message message = objectMapper.readValue(entity.getPayload(), Message.class);
            deliverImmediately(message, entity.getRoutingKey());

            entity.setStatus(DeliveryStatus.DELIVERED);
            entity.setDeliveredAt(LocalDateTime.now());
            entity.setUpdatedAt(entity.getDeliveredAt());
            entity.setLastError(null);
            pendingMessageRepository.save(entity);
            log.info("Message {} delivered after {} attempts", entity.getMessageId(), entity.getAttemptCount() + 1);
        } catch (Exception e) {
            handleFailure(entity, e);
        }
    }

    private void handleFailure(PendingMessageEntity entity, Exception exception) {
        int attempts = entity.getAttemptCount() + 1;
        entity.setAttemptCount(attempts);
        entity.setLastError(exception.getMessage());
        entity.setUpdatedAt(LocalDateTime.now());

        if (attempts >= maxDeliveryAttempts) {
            entity.setStatus(DeliveryStatus.FAILED);
            pendingMessageRepository.save(entity);
            log.error("Message {} marked as FAILED after {} attempts: {}",
                    entity.getMessageId(), attempts, exception.getMessage());
            return;
        }

        entity.setStatus(DeliveryStatus.WAITING);
        entity.setNextAttemptAt(LocalDateTime.now().plusSeconds(deliveryDelaySeconds));
        pendingMessageRepository.save(entity);
        log.warn("Message {} delivery failed (attempt {}/{}). Next try at {}",
                entity.getMessageId(), attempts, maxDeliveryAttempts, entity.getNextAttemptAt());
        enqueueForDelivery(entity);
    }

    private void scheduleMessageForDelivery(Message message, String routingKey) {
        Message normalizedMessage = normalizeMessage(message);
        PendingMessageEntity entity = persistPendingMessage(normalizedMessage, routingKey);
        enqueueForDelivery(entity);
    }

    private PendingMessageEntity persistPendingMessage(Message message, String routingKey) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextAttempt = now.plusSeconds(deliveryDelaySeconds);
        String payload = serializeMessage(message);

        PendingMessageEntity entity = pendingMessageRepository.findByMessageId(message.getId())
                .map(existing -> {
                    existing.setPayload(payload);
                    existing.setRoutingKey(resolveRoutingKey(routingKey));
                    existing.setStatus(DeliveryStatus.WAITING);
                    existing.setAttemptCount(0);
                    existing.setLastError(null);
                    existing.setDeliveredAt(null);
                    existing.setNextAttemptAt(nextAttempt);
                    existing.setUpdatedAt(now);
                    return existing;
                })
                .orElseGet(() -> PendingMessageEntity.builder()
                        .messageId(message.getId())
                        .payload(payload)
                        .routingKey(resolveRoutingKey(routingKey))
                        .status(DeliveryStatus.WAITING)
                        .attemptCount(0)
                        .nextAttemptAt(nextAttempt)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());

        PendingMessageEntity saved = pendingMessageRepository.save(entity);
        log.debug("Message {} scheduled for {} (attempt #{})",
                saved.getMessageId(), saved.getNextAttemptAt(), saved.getAttemptCount() + 1);
        return saved;
    }

    private void enqueueForDelivery(PendingMessageEntity entity) {
        long triggerMillis = entity.getNextAttemptAt()
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli();
        deliveryQueue.offer(new DelayedDeliveryTask(entity.getId(), triggerMillis));
    }

    private void restorePendingMessages() {
        List<PendingMessageEntity> pendingMessages = pendingMessageRepository.findByStatusIn(
                List.of(DeliveryStatus.WAITING, DeliveryStatus.IN_PROGRESS));

        if (pendingMessages.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        pendingMessages.forEach(entity -> {
            if (entity.getStatus() == DeliveryStatus.IN_PROGRESS) {
                entity.setStatus(DeliveryStatus.WAITING);
                entity.setUpdatedAt(now);
                pendingMessageRepository.save(entity);
            }
            enqueueForDelivery(entity);
        });

        log.info("Recovered {} messages awaiting delivery from previous run", pendingMessages.size());
    }

    private void deliverImmediately(Message message, String routingKey) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
        properties.setMessageId(message.getId());
        properties.setPriority(getPriorityValue(message.getPriority()));

        rabbitTemplate.convertAndSend(
                exchangeName,
                resolveRoutingKey(routingKey),
                message,
                msg -> {
                    msg.getMessageProperties().setMessageId(message.getId());
                    msg.getMessageProperties().setPriority(getPriorityValue(message.getPriority()));
                    return msg;
                }
        );
    }

    private Message normalizeMessage(Message message) {
        Message normalized = message;
        if (normalized.getId() == null) {
            normalized.setId(UUID.randomUUID().toString());
        }
        if (normalized.getTimestamp() == null) {
            normalized.setTimestamp(LocalDateTime.now());
        }
        return normalized;
    }

    private String serializeMessage(Message message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize message " + message.getId(), e);
        }
    }

    private String resolveRoutingKey(String routingKey) {
        return routingKey != null ? routingKey : "message.routing.key";
    }

    private int getPriorityValue(String priority) {
        if (priority == null) return 0;
        return switch (priority.toLowerCase()) {
            case "high" -> 10;
            case "medium" -> 5;
            case "low" -> 1;
            default -> 0;
        };
    }

    private static final class DelayedDeliveryTask implements Delayed {
        private final Long entityId;
        private final long triggerTimeMillis;

        private DelayedDeliveryTask(Long entityId, long triggerTimeMillis) {
            this.entityId = entityId;
            this.triggerTimeMillis = triggerTimeMillis;
        }

        public Long getEntityId() {
            return entityId;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = triggerTimeMillis - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if (this == o) {
                return 0;
            }
            long otherTrigger = ((DelayedDeliveryTask) o).triggerTimeMillis;
            return Long.compare(this.triggerTimeMillis, otherTrigger);
        }
    }
}