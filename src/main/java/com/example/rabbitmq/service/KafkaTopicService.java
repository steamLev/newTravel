package com.example.rabbitmq.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для управления топиками Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaTopicService {

    private final KafkaAdmin kafkaAdmin;

    /**
     * Создает топик с базовыми настройками
     */
    public void createTopic(String topicName, int partitions, int replicas) {
        createTopicWithConfig(topicName, partitions, replicas, new HashMap<>());
    }

    /**
     * Создает топик с расширенными настройками
     */
    public void createTopicWithConfig(String topicName, int partitions, int replicas, 
                                    Map<String, String> configs) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            
            // Проверяем, существует ли топик
            if (topicExists(adminClient, topicName)) {
                log.warn("Топик {} уже существует", topicName);
                return;
            }

            // Создаем топик
            NewTopic newTopic = TopicBuilder.name(topicName)
                    .partitions(partitions)
                    .replicas(replicas)
                    .configs(configs)
                    .build();

            CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
            
            // Ждем завершения создания
            result.all().get(30, TimeUnit.SECONDS);
            log.info("✅ Топик {} успешно создан с {} партициями и {} репликами", 
                    topicName, partitions, replicas);
                    
        } catch (Exception e) {
            log.error("❌ Ошибка при создании топика {}: {}", topicName, e.getMessage(), e);
            throw new RuntimeException("Failed to create topic: " + topicName, e);
        }
    }

    /**
     * Создает топик для событий
     */
    public void createEventsTopic(String topicName, int partitions) {
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "2592000000"); // 30 дней
        configs.put(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, "delete");
        configs.put(TopicConfig.SEGMENT_MS_CONFIG, "86400000"); // 1 день
        
        createTopicWithConfig(topicName, partitions, 1, configs);
    }

    /**
     * Создает топик для логов
     */
    public void createLogsTopic(String topicName, int partitions) {
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "2592000000"); // 30 дней
        configs.put(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, "delete");
        configs.put(TopicConfig.SEGMENT_MS_CONFIG, "86400000"); // 1 день
        
        createTopicWithConfig(topicName, partitions, 1, configs);
    }

    /**
     * Создает топик для аудита
     */
    public void createAuditTopic(String topicName, int partitions) {
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "31536000000"); // 1 год
        configs.put(TopicConfig.COMPRESSION_TYPE_CONFIG, "gzip");
        configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, "delete");
        configs.put(TopicConfig.SEGMENT_MS_CONFIG, "604800000"); // 7 дней
        
        createTopicWithConfig(topicName, partitions, 1, configs);
    }

    /**
     * Создает топик для метрик
     */
    public void createMetricsTopic(String topicName, int partitions) {
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "604800000"); // 7 дней
        configs.put(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, "delete");
        
        createTopicWithConfig(topicName, partitions, 1, configs);
    }

    /**
     * Проверяет существование топика
     */
    public boolean topicExists(String topicName) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            return topicExists(adminClient, topicName);
        } catch (Exception e) {
            log.error("Ошибка при проверке существования топика {}: {}", topicName, e.getMessage());
            return false;
        }
    }

    private boolean topicExists(AdminClient adminClient, String topicName) throws Exception {
        ListTopicsResult topics = adminClient.listTopics();
        return topics.names().get().contains(topicName);
    }

    /**
     * Получает список всех топиков
     */
    public List<String> listTopics() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            ListTopicsResult topics = adminClient.listTopics();
            return new ArrayList<>(topics.names().get());
        } catch (Exception e) {
            log.error("Ошибка при получении списка топиков: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Получает описание топика
     */
    public TopicDescription getTopicDescription(String topicName) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            return adminClient.describeTopics(Collections.singletonList(topicName))
                    .values()
                    .get(topicName)
                    .get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Ошибка при получении описания топика {}: {}", topicName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Удаляет топик
     */
    public void deleteTopic(String topicName) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            
            if (!topicExists(adminClient, topicName)) {
                log.warn("Топик {} не существует", topicName);
                return;
            }

            DeleteTopicsResult result = adminClient.deleteTopics(Collections.singletonList(topicName));
            result.all().get(30, TimeUnit.SECONDS);
            log.info("✅ Топик {} удален", topicName);
        } catch (Exception e) {
            log.error("❌ Ошибка при удалении топика {}: {}", topicName, e.getMessage(), e);
            throw new RuntimeException("Failed to delete topic: " + topicName, e);
        }
    }

    /**
     * Получает статистику топиков
     */
    public Map<String, Object> getTopicsStats() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            List<String> topics = listTopics();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTopics", topics.size());
            stats.put("topics", topics);
            
            // Подсчитываем партиции
            int totalPartitions = 0;
            for (String topicName : topics) {
                TopicDescription description = getTopicDescription(topicName);
                if (description != null) {
                    totalPartitions += description.partitions().size();
                }
            }
            stats.put("totalPartitions", totalPartitions);
            
            return stats;
        } catch (Exception e) {
            log.error("Ошибка при получении статистики топиков: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
}