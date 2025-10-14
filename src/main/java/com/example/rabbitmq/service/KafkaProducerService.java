package com.example.rabbitmq.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Сервис для отправки сообщений в Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Отправляет сообщение в топик
     */
    public CompletableFuture<SendResult<String, String>> sendMessage(String topic, String message) {
        log.info("📤 Отправляем сообщение в топик '{}': {}", topic, message);
        
        return kafkaTemplate.send(topic, message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("✅ Сообщение успешно отправлено в топик '{}', offset: {}", 
                                topic, result.getRecordMetadata().offset());
                    } else {
                        log.error("❌ Ошибка при отправке сообщения в топик '{}': {}", topic, ex.getMessage());
                    }
                });
    }

    /**
     * Отправляет тестовое сообщение
     */
    public void sendTestMessage() {
        String message = "Тестовое сообщение от Spring Boot приложения: " + System.currentTimeMillis();
        sendMessage("test-topic", message);
    }

    /**
     * Отправляет user event
     */
    public void sendUserEvent(String userId, String action) {
        String message = String.format("{\"userId\":\"%s\",\"action\":\"%s\",\"timestamp\":%d}", 
                userId, action, System.currentTimeMillis());
        sendMessage("user-events", message);
    }

    /**
     * Отправляет order event
     */
    public void sendOrderEvent(String orderId, String status) {
        String message = String.format("{\"orderId\":\"%s\",\"status\":\"%s\",\"timestamp\":%d}", 
                orderId, status, System.currentTimeMillis());
        sendMessage("order-events", message);
    }
}