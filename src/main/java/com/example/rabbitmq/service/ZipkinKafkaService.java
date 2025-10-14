package com.example.rabbitmq.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Сервис для работы с Kafka через Zipkin трассировку
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZipkinKafkaService {

    private final StreamBridge streamBridge;

    /**
     * Отправляет сообщение в Kafka с трассировкой
     */
    public CompletableFuture<Void> sendMessage(String topic, String message) {
        log.info("📤 [TRACED] Отправляем сообщение в топик '{}': {}", topic, message);
        
        return CompletableFuture.runAsync(() -> {
            try {
                // Используем StreamBridge для отправки с трассировкой
                streamBridge.send("output", message);
                log.info("✅ [TRACED] Сообщение успешно отправлено в топик '{}'", topic);
            } catch (Exception e) {
                log.error("❌ [TRACED] Ошибка при отправке сообщения в топик '{}': {}", topic, e.getMessage(), e);
                throw new RuntimeException("Failed to send message", e);
            }
        });
    }

    /**
     * Отправляет тестовое сообщение с трассировкой
     */
    public void sendTestMessage() {
        String message = "Тестовое сообщение с Zipkin трассировкой: " + System.currentTimeMillis();
        sendMessage("messages-topic", message);
    }

    /**
     * Отправляет user event с трассировкой
     */
    public void sendUserEvent(String userId, String action) {
        String message = String.format("{\"userId\":\"%s\",\"action\":\"%s\",\"timestamp\":%d,\"traced\":true}", 
                userId, action, System.currentTimeMillis());
        sendMessage("messages-topic", message);
    }

    /**
     * Слушатель Kafka сообщений с трассировкой
     */
    @KafkaListener(topics = "messages-topic", groupId = "spring-app-group")
    public void handleMessage(@Payload String message,
                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                             @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                             @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("📨 [TRACED] Получено сообщение из топика '{}': {}", topic, message);
        log.info("📍 [TRACED] Partition: {}, Offset: {}", partition, offset);
        
        // Здесь можно добавить дополнительную обработку с трассировкой
        processMessage(message);
    }

    /**
     * Обработка сообщения с трассировкой
     */
    private void processMessage(String message) {
        log.info("🔄 [TRACED] Обрабатываем сообщение: {}", message);
        
        try {
            // Симулируем обработку
            Thread.sleep(100);
            log.info("✅ [TRACED] Сообщение обработано успешно");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ [TRACED] Ошибка при обработке сообщения", e);
        }
    }
}