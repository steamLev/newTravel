package com.example.rabbitmq.controller;

import com.example.rabbitmq.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для тестирования Kafka
 */
@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
public class KafkaTestController {

    private final KafkaProducerService kafkaProducerService;

    /**
     * Отправляет тестовое сообщение
     */
    @PostMapping("/test")
    public ResponseEntity<String> sendTestMessage() {
        try {
            kafkaProducerService.sendTestMessage();
            return ResponseEntity.ok("Тестовое сообщение отправлено в test-topic");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Ошибка при отправке: " + e.getMessage());
        }
    }

    /**
     * Отправляет user event
     */
    @PostMapping("/user-event")
    public ResponseEntity<String> sendUserEvent(@RequestParam String userId, 
                                              @RequestParam String action) {
        try {
            kafkaProducerService.sendUserEvent(userId, action);
            return ResponseEntity.ok("User event отправлен в user-events топик");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Ошибка при отправке: " + e.getMessage());
        }
    }

    /**
     * Отправляет order event
     */
    @PostMapping("/order-event")
    public ResponseEntity<String> sendOrderEvent(@RequestParam String orderId, 
                                               @RequestParam String status) {
        try {
            kafkaProducerService.sendOrderEvent(orderId, status);
            return ResponseEntity.ok("Order event отправлен в order-events топик");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Ошибка при отправке: " + e.getMessage());
        }
    }

    /**
     * Отправляет произвольное сообщение в топик
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam String topic, 
                                            @RequestParam String message) {
        try {
            kafkaProducerService.sendMessage(topic, message);
            return ResponseEntity.ok("Сообщение отправлено в топик: " + topic);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Ошибка при отправке: " + e.getMessage());
        }
    }
}