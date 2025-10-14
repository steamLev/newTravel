package com.example.rabbitmq.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Слушатель Kafka сообщений
 */
@Slf4j
@Component
public class KafkaMessageListener {

    @KafkaListener(topics = "test-topic", groupId = "my-group")
    public void handleTestTopic(@Payload String message,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                               @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("📨 Получено сообщение из топика '{}': {}", topic, message);
        log.info("📍 Partition: {}, Offset: {}", partition, offset);
    }

    @KafkaListener(topics = "user-events", groupId = "my-group")
    public void handleUserEvents(@Payload String message,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("👤 User event из топика '{}': {}", topic, message);
    }

    @KafkaListener(topics = "order-events", groupId = "my-group")
    public void handleOrderEvents(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("🛒 Order event из топика '{}': {}", topic, message);
    }
}