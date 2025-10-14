package com.example.rabbitmq.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для мониторинга здоровья приложения
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthMonitoringService implements HealthIndicator {

    private final ConnectionFactory connectionFactory;
    private final MessageSenderService messageSenderService;

    @Override
    public Health health() {
        try {
            // Проверяем RabbitMQ соединение
            if (!isRabbitMQHealthy()) {
                return Health.down()
                        .withDetail("rabbitmq", "Connection failed")
                        .withDetail("timestamp", System.currentTimeMillis())
                        .build();
            }

            // Проверяем доступность каналов
            if (!messageSenderService.canSendMessages()) {
                return Health.down()
                        .withDetail("rabbitmq", "No available channels")
                        .withDetail("timestamp", System.currentTimeMillis())
                        .build();
            }

            // Проверяем память
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

            if (memoryUsagePercent > 90) {
                return Health.down()
                        .withDetail("memory", "High memory usage: " + String.format("%.2f", memoryUsagePercent) + "%")
                        .withDetail("used", usedMemory)
                        .withDetail("max", maxMemory)
                        .withDetail("timestamp", System.currentTimeMillis())
                        .build();
            }

            return Health.up()
                    .withDetail("rabbitmq", "Connected")
                    .withDetail("channels", "Available")
                    .withDetail("memory", String.format("%.2f", memoryUsagePercent) + "%")
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("Health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        }
    }

    private boolean isRabbitMQHealthy() {
        try {
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    connectionFactory.createConnection().close();
                    return true;
                } catch (Exception e) {
                    log.warn("RabbitMQ health check failed: {}", e.getMessage());
                    return false;
                }
            });
            
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("RabbitMQ health check timeout or failed: {}", e.getMessage());
            return false;
        }
    }
}