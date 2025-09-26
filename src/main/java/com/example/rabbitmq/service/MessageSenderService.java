package com.example.rabbitmq.service;

import com.example.rabbitmq.model.Message;
import com.example.rabbitmq.model.MessageBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSenderService {

    private final RabbitTemplate rabbitTemplate;
    private final ChannelMonitorService channelMonitorService;
    
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    
    @Value("${rabbitmq.queue.name}")
    private String queueName;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Отправляет список сообщений в RabbitMQ с проверкой доступности каналов
     */
    public CompletableFuture<MessageBatch> sendMessageBatch(MessageBatch messageBatch) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Starting to send batch {} with {} messages", 
                    messageBatch.getBatchId(), messageBatch.getTotalCount());
            
            // Проверяем доступность каналов
            if (!channelMonitorService.areChannelsAvailable()) {
                log.warn("Channels not available for batch {}", messageBatch.getBatchId());
                throw new RuntimeException("No available channels in RabbitMQ broker");
            }
            
            try {
                // Отправляем сообщения
                List<CompletableFuture<Void>> sendFutures = messageBatch.getMessages().stream()
                        .map(message -> sendSingleMessage(message, messageBatch.getRoutingKey()))
                        .collect(Collectors.toList());
                
                // Ждем завершения всех отправок
                CompletableFuture.allOf(sendFutures.toArray(new CompletableFuture[0])).join();
                
                log.info("Successfully sent batch {} with {} messages", 
                        messageBatch.getBatchId(), messageBatch.getTotalCount());
                
                return messageBatch;
                
            } catch (Exception e) {
                log.error("Error sending batch {}", messageBatch.getBatchId(), e);
                throw new RuntimeException("Failed to send message batch", e);
            }
        }, executorService);
    }

    /**
     * Отправляет одно сообщение с retry логикой
     */
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Void> sendSingleMessage(Message message, String routingKey) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Дополнительная проверка перед отправкой
                if (!channelMonitorService.areChannelsAvailable()) {
                    throw new RuntimeException("No available channels");
                }
                
                // Настройка свойств сообщения
                MessageProperties properties = new MessageProperties();
                properties.setContentType("application/json");
                properties.setMessageId(message.getId());
                properties.setPriority(getPriorityValue(message.getPriority()));
                
                // Отправка сообщения
                rabbitTemplate.convertAndSend(
                        exchangeName,
                        routingKey != null ? routingKey : "message.routing.key",
                        message,
                        msg -> {
                            msg.getMessageProperties().setMessageId(message.getId());
                            msg.getMessageProperties().setPriority(getPriorityValue(message.getPriority()));
                            return msg;
                        }
                );
                
                log.debug("Message {} sent successfully", message.getId());
                
            } catch (Exception e) {
                log.error("Failed to send message {}", message.getId(), e);
                throw new RuntimeException("Failed to send message: " + message.getId(), e);
            }
        }, executorService);
    }

    /**
     * Отправляет сообщения асинхронно с контролем каналов
     */
    public CompletableFuture<List<Message>> sendMessagesAsync(List<Message> messages, String routingKey) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Sending {} messages asynchronously", messages.size());
            
            if (!channelMonitorService.areChannelsAvailable()) {
                throw new RuntimeException("No available channels in RabbitMQ broker");
            }
            
            List<CompletableFuture<Message>> futures = messages.stream()
                    .map(message -> sendSingleMessage(message, routingKey)
                            .thenApply(v -> message))
                    .collect(Collectors.toList());
            
            return futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        }, executorService);
    }

    /**
     * Проверяет статус каналов перед отправкой
     */
    public boolean canSendMessages() {
        return channelMonitorService.areChannelsAvailable();
    }

    /**
     * Получает статус каналов
     */
    public String getChannelStatus() {
        return channelMonitorService.getChannelStatus().toString();
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
}