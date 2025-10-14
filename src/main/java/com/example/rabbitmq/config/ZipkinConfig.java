package com.example.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация Zipkin для трассировки Kafka с Micrometer Tracing
 * Автоматическая трассировка через Micrometer Tracing
 */
@Slf4j
@Configuration
@EnableKafka
public class ZipkinConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Producer Factory с автоматической трассировкой
     */
    @Bean
    public ProducerFactory<String, String> tracingProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        
        log.info("Tracing Producer Factory configured with Micrometer Tracing + Zipkin (automatic)");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Consumer Factory с автоматической трассировкой
     */
    @Bean
    public ConsumerFactory<String, String> tracingConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        log.info("Tracing Consumer Factory configured with Micrometer Tracing + Zipkin (automatic)");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Kafka Template с автоматической трассировкой
     */
    @Bean
    public KafkaTemplate<String, String> tracingKafkaTemplate() {
        return new KafkaTemplate<>(tracingProducerFactory());
    }

    /**
     * Kafka Listener Container Factory с автоматической трассировкой
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> tracingKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(tracingConsumerFactory());
        factory.setConcurrency(2);
        
        // Настройки для трассировки
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        log.info("Tracing Kafka Listener Container Factory configured with Micrometer Tracing + Zipkin (automatic)");
        return factory;
    }
}