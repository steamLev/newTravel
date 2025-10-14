package com.example.rabbitmq.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Контроллер для управления жизненным циклом приложения
 */
@Slf4j
@RestController
@RequestMapping("/api/lifecycle")
@RequiredArgsConstructor
public class LifecycleController {

    private volatile boolean isShuttingDown = false;

    /**
     * Получить статус приложения
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
            "status", isShuttingDown ? "SHUTTING_DOWN" : "RUNNING",
            "timestamp", System.currentTimeMillis(),
            "uptime", getUptime(),
            "memory", getMemoryInfo()
        ));
    }

    /**
     * Graceful shutdown
     */
    @PostMapping("/shutdown")
    public ResponseEntity<String> gracefulShutdown() {
        if (isShuttingDown) {
            return ResponseEntity.badRequest().body("Application is already shutting down");
        }

        log.info("🛑 Initiating graceful shutdown...");
        isShuttingDown = true;

        // Здесь можно добавить логику для graceful shutdown
        // Например, остановка приема новых сообщений, завершение обработки текущих

        new Thread(() -> {
            try {
                Thread.sleep(5000); // Даем время для завершения текущих операций
                log.info("🔄 Shutting down application...");
                System.exit(0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Shutdown interrupted", e);
            }
        }).start();

        return ResponseEntity.ok("Graceful shutdown initiated");
    }

    /**
     * Проверка готовности к shutdown
     */
    @GetMapping("/ready-for-shutdown")
    public ResponseEntity<Map<String, Object>> readyForShutdown() {
        boolean ready = !isShuttingDown && isHealthy();
        return ResponseEntity.ok(Map.of(
            "ready", ready,
            "shutting_down", isShuttingDown,
            "healthy", isHealthy(),
            "timestamp", System.currentTimeMillis()
        ));
    }

    private boolean isHealthy() {
        // Простая проверка здоровья
        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double memoryUsage = (double) usedMemory / maxMemory * 100;
            return memoryUsage < 95; // Считаем нездоровым при использовании >95% памяти
        } catch (Exception e) {
            log.error("Health check failed", e);
            return false;
        }
    }

    private long getUptime() {
        return System.currentTimeMillis() - getStartTime();
    }

    private long getStartTime() {
        // Простая реализация - в реальном приложении лучше использовать Spring Boot Actuator
        return System.currentTimeMillis() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }

    private Map<String, Object> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return Map.of(
            "max", maxMemory,
            "total", totalMemory,
            "free", freeMemory,
            "used", usedMemory,
            "usage_percent", String.format("%.2f", (double) usedMemory / maxMemory * 100)
        );
    }
}