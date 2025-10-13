package com.example.rabbitmq.controller;

import com.example.rabbitmq.test.RabbitListenerTest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для тестирования RabbitMQ
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final RabbitListenerTest rabbitListenerTest;

    /**
     * Тестирует отправку сообщения в BINARY_QUEUE
     */
    @PostMapping("/rabbit-listener")
    public ResponseEntity<String> testRabbitListener() {
        try {
            rabbitListenerTest.sendTestMessage();
            return ResponseEntity.ok("Тестовое сообщение отправлено в BINARY_QUEUE");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Ошибка при тестировании: " + e.getMessage());
        }
    }
}