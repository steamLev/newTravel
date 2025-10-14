package com.example.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Consul
 * Настройки Consul выполняются через application-consul.yml
 */
@Slf4j
@Configuration
public class ConsulConfig {
    
    // Конфигурация Consul выполняется через application-consul.yml
    // Spring Cloud Consul автоматически подхватывает настройки из конфигурации
}