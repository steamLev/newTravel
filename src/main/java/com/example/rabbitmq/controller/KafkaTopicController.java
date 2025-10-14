package com.example.rabbitmq.controller;

import com.example.rabbitmq.service.KafkaTopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления топиками Kafka
 */
@Slf4j
@RestController
@RequestMapping("/api/kafka/topics")
@RequiredArgsConstructor
public class KafkaTopicController {

    private final KafkaTopicService kafkaTopicService;

    /**
     * Создает топик с базовыми настройками
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createTopic(
            @RequestParam String topicName,
            @RequestParam(defaultValue = "3") int partitions,
            @RequestParam(defaultValue = "1") int replicas) {
        
        log.info("Создание топика: {} с {} партициями и {} репликами", topicName, partitions, replicas);
        
        try {
            kafkaTopicService.createTopic(topicName, partitions, replicas);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Топик успешно создан",
                "topicName", topicName,
                "partitions", partitions,
                "replicas", replicas
            ));
        } catch (Exception e) {
            log.error("Ошибка при создании топика {}: {}", topicName, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "error",
                        "message", "Ошибка при создании топика: " + e.getMessage(),
                        "topicName", topicName
                    ));
        }
    }

    /**
     * Создает топик для событий
     */
    @PostMapping("/create/events")
    public ResponseEntity<Map<String, Object>> createEventsTopic(
            @RequestParam String topicName,
            @RequestParam(defaultValue = "6") int partitions) {
        
        log.info("Создание топика для событий: {} с {} партициями", topicName, partitions);
        
        try {
            kafkaTopicService.createEventsTopic(topicName, partitions);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Топик для событий успешно создан",
                "topicName", topicName,
                "partitions", partitions,
                "type", "events"
            ));
        } catch (Exception e) {
            log.error("Ошибка при создании топика событий {}: {}", topicName, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "error",
                        "message", "Ошибка при создании топика событий: " + e.getMessage(),
                        "topicName", topicName
                    ));
        }
    }

    /**
     * Создает топик для логов
     */
    @PostMapping("/create/logs")
    public ResponseEntity<Map<String, Object>> createLogsTopic(
            @RequestParam String topicName,
            @RequestParam(defaultValue = "12") int partitions) {
        
        log.info("Создание топика для логов: {} с {} партициями", topicName, partitions);
        
        try {
            kafkaTopicService.createLogsTopic(topicName, partitions);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Топик для логов успешно создан",
                "topicName", topicName,
                "partitions", partitions,
                "type", "logs"
            ));
        } catch (Exception e) {
            log.error("Ошибка при создании топика логов {}: {}", topicName, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "error",
                        "message", "Ошибка при создании топика логов: " + e.getMessage(),
                        "topicName", topicName
                    ));
        }
    }

    /**
     * Создает топик для аудита
     */
    @PostMapping("/create/audit")
    public ResponseEntity<Map<String, Object>> createAuditTopic(
            @RequestParam String topicName,
            @RequestParam(defaultValue = "12") int partitions) {
        
        log.info("Создание топика для аудита: {} с {} партициями", topicName, partitions);
        
        try {
            kafkaTopicService.createAuditTopic(topicName, partitions);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Топик для аудита успешно создан",
                "topicName", topicName,
                "partitions", partitions,
                "type", "audit"
            ));
        } catch (Exception e) {
            log.error("Ошибка при создании топика аудита {}: {}", topicName, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "error",
                        "message", "Ошибка при создании топика аудита: " + e.getMessage(),
                        "topicName", topicName
                    ));
        }
    }

    /**
     * Получает список всех топиков
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listTopics() {
        try {
            List<String> topics = kafkaTopicService.listTopics();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "topics", topics,
                "count", topics.size()
            ));
        } catch (Exception e) {
            log.error("Ошибка при получении списка топиков: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "error",
                        "message", "Ошибка при получении списка топиков: " + e.getMessage()
                    ));
        }
    }

    /**
     * Получает описание топика
     */
    @GetMapping("/describe/{topicName}")
    public ResponseEntity<Map<String, Object>> describeTopic(@PathVariable String topicName) {
        try {
            TopicDescription description = kafkaTopicService.getTopicDescription(topicName);
            
            if (description == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> topicInfo = new HashMap<>();
            topicInfo.put("name", description.name());
            topicInfo.put("partitions", description.partitions().size());
            topicInfo.put("isInternal", description.isInternal());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "topic", topicInfo
            ));
        } catch (Exception e) {
            log.error("Ошибка при получении описания топика {}: {}", topicName, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "error",
                        "message", "Ошибка при получении описания топика: " + e.getMessage(),
                        "topicName", topicName
                    ));
        }
    }

    /**
     * Проверяет существование топика
     */
    @GetMapping("/exists/{topicName}")
    public ResponseEntity<Map<String, Object>> topicExists(@PathVariable String topicName) {
        try {
            boolean exists = kafkaTopicService.topicExists(topicName);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "topicName", topicName,
                "exists", exists
            ));
        } catch (Exception e) {
            log.error("Ошибка при проверке существования топика {}: {}", topicName, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "error",
                        "message", "Ошибка при проверке существования топика: " + e.getMessage(),
                        "topicName", topicName
                    ));
        }
    }

    /**
     * Удаляет топик
     */
    @DeleteMapping("/{topicName}")
    public ResponseEntity<Map<String, Object>> deleteTopic(@PathVariable String topicName) {
        log.info("Удаление топика: {}", topicName);
        
        try {
            kafkaTopicService.deleteTopic(topicName);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Топик успешно удален",
                "topicName", topicName
            ));
        } catch (Exception e) {
            log.error("Ошибка при удалении топика {}: {}", topicName, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "error",
                        "message", "Ошибка при удалении топика: " + e.getMessage(),
                        "topicName", topicName
                    ));
        }
    }

    /**
     * Получает статистику топиков
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTopicsStats() {
        try {
            Map<String, Object> stats = kafkaTopicService.getTopicsStats();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "stats", stats
            ));
        } catch (Exception e) {
            log.error("Ошибка при получении статистики топиков: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "error",
                        "message", "Ошибка при получении статистики топиков: " + e.getMessage()
                    ));
        }
    }
}