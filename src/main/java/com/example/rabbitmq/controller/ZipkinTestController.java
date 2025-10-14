package com.example.rabbitmq.controller;

import com.example.rabbitmq.service.ZipkinKafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Контроллер для тестирования Zipkin трассировки с Kafka
 */
@Slf4j
@RestController
@RequestMapping("/api/zipkin")
@RequiredArgsConstructor
public class ZipkinTestController {

    private final ZipkinKafkaService zipkinKafkaService;

    /**
     * Отправляет тестовое сообщение с трассировкой
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> sendTestMessage() {
        log.info("🧪 [TRACED] Тестирование Zipkin трассировки с Kafka");
        
        try {
            zipkinKafkaService.sendTestMessage();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Тестовое сообщение отправлено с Zipkin трассировкой",
                "timestamp", System.currentTimeMillis(),
                "traced", true
            ));
        } catch (Exception e) {
            log.error("❌ [TRACED] Ошибка при тестировании Zipkin", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "error",
                        "message", "Ошибка при тестировании: " + e.getMessage(),
                        "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * Отправляет user event с трассировкой
     */
    @PostMapping("/user-event")
    public ResponseEntity<Map<String, Object>> sendUserEvent(@RequestParam String userId, 
                                                            @RequestParam String action) {
        log.info("👤 [TRACED] Отправка user event с трассировкой: userId={}, action={}", userId, action);
        
        try {
            zipkinKafkaService.sendUserEvent(userId, action);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "User event отправлен с Zipkin трассировкой",
                "userId", userId,
                "action", action,
                "timestamp", System.currentTimeMillis(),
                "traced", true
            ));
        } catch (Exception e) {
            log.error("❌ [TRACED] Ошибка при отправке user event", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "error",
                        "message", "Ошибка при отправке user event: " + e.getMessage(),
                        "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * Отправляет произвольное сообщение с трассировкой
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestParam String topic, 
                                                          @RequestParam String message) {
        log.info("📤 [TRACED] Отправка произвольного сообщения: topic={}, message={}", topic, message);
        
        try {
            CompletableFuture<Void> future = zipkinKafkaService.sendMessage(topic, message);
            future.get(); // Ждем завершения
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Сообщение отправлено с Zipkin трассировкой",
                "topic", topic,
                "content", message,
                "timestamp", System.currentTimeMillis(),
                "traced", true
            ));
        } catch (Exception e) {
            log.error("❌ [TRACED] Ошибка при отправке сообщения", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "error",
                        "message", "Ошибка при отправке сообщения: " + e.getMessage(),
                        "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * Получает информацию о трассировке
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getTracingInfo() {
        return ResponseEntity.ok(Map.of(
            "tracing", "enabled",
            "zipkin_url", "http://localhost:9411",
            "kafka_topic", "messages-topic",
            "sampling_rate", "100%",
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Тестирует цепочку трассировки (отправка -> получение -> обработка)
     */
    @PostMapping("/chain-test")
    public ResponseEntity<Map<String, Object>> testTracingChain() {
        log.info("🔗 [TRACED] Тестирование цепочки трассировки");
        
        try {
            // Отправляем несколько сообщений для создания цепочки трассировки
            for (int i = 1; i <= 3; i++) {
                String message = String.format("Chain test message %d: %d", i, System.currentTimeMillis());
                zipkinKafkaService.sendMessage("messages-topic", message);
                Thread.sleep(500); // Небольшая задержка между сообщениями
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Цепочка трассировки протестирована",
                "messages_sent", 3,
                "timestamp", System.currentTimeMillis(),
                "traced", true
            ));
        } catch (Exception e) {
            log.error("❌ [TRACED] Ошибка при тестировании цепочки трассировки", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "error",
                        "message", "Ошибка при тестировании цепочки: " + e.getMessage(),
                        "timestamp", System.currentTimeMillis()
                    ));
        }
    }
}