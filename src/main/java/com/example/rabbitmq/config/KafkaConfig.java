package com.example.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация Kafka с правильным созданием топиков
 */
@Slf4j
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Включаем автоматическое создание топиков
        configs.put("auto.create.topics.enable", "true");
        return new KafkaAdmin(configs);
    }

    @Bean
    public AdminClient kafkaAdminClient() {
        return AdminClient.create(kafkaAdmin().getConfigurationProperties());
    }

    // ===== ОСНОВНЫЕ ТОПИКИ =====

    /**
     * Тестовый топик - базовые настройки
     */
    @Bean
    public NewTopic testTopic() {
        log.info("Создаем тестовый топик: test-topic");
        return TopicBuilder.name("test-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Топик для сообщений с трассировкой
     */
    @Bean
    public NewTopic messagesTopic() {
        log.info("Создаем топик для сообщений: messages-topic");
        return TopicBuilder.name("messages-topic")
                .partitions(6)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 дней
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .build();
    }

    /**
     * Топик для Zipkin трассировок
     */
    @Bean
    public NewTopic zipkinTopic() {
        log.info("Создаем топик для Zipkin: zipkin");
        return TopicBuilder.name("zipkin")
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "86400000") // 1 день
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "gzip")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .build();
    }

    // ===== БИЗНЕС ТОПИКИ =====

    /**
     * Топик для пользовательских событий
     */
    @Bean
    public NewTopic userEventsTopic() {
        log.info("Создаем топик для пользовательских событий: user-events");
        return TopicBuilder.name("user-events")
                .partitions(6)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000") // 30 дней
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .config(TopicConfig.SEGMENT_MS_CONFIG, "86400000") // 1 день
                .build();
    }

    /**
     * Топик для заказов
     */
    @Bean
    public NewTopic orderEventsTopic() {
        log.info("Создаем топик для заказов: order-events");
        return TopicBuilder.name("order-events")
                .partitions(6)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "7776000000") // 90 дней
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .config(TopicConfig.SEGMENT_MS_CONFIG, "86400000") // 1 день
                .build();
    }

    /**
     * Топик для системных событий
     */
    @Bean
    public NewTopic systemEventsTopic() {
        log.info("Создаем топик для системных событий: system-events");
        return TopicBuilder.name("system-events")
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 дней
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "gzip")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .build();
    }

    // ===== АУДИТ И ЛОГИ =====

    /**
     * Топик для аудита
     */
    @Bean
    public NewTopic auditTopic() {
        log.info("Создаем топик для аудита: audit-events");
        return TopicBuilder.name("audit-events")
                .partitions(12)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "31536000000") // 1 год
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "gzip")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .config(TopicConfig.SEGMENT_MS_CONFIG, "604800000") // 7 дней
                .build();
    }

    /**
     * Топик для логов приложения
     */
    @Bean
    public NewTopic applicationLogsTopic() {
        log.info("Создаем топик для логов приложения: application-logs");
        return TopicBuilder.name("application-logs")
                .partitions(12)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000") // 30 дней
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .config(TopicConfig.SEGMENT_MS_CONFIG, "86400000") // 1 день
                .build();
    }

    // ===== МЕТРИКИ =====

    /**
     * Топик для метрик
     */
    @Bean
    public NewTopic metricsTopic() {
        log.info("Создаем топик для метрик: metrics");
        return TopicBuilder.name("metrics")
                .partitions(6)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 дней
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .build();
    }
}