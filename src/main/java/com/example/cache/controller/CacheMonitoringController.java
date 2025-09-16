package com.example.cache.controller;

import com.example.cache.config.CacheMonitoringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для мониторинга кеша
 */
@RestController
@RequestMapping("/api/cache")
public class CacheMonitoringController {

    @Autowired
    private CacheMonitoringConfig cacheMonitoringConfig;

    /**
     * Получить статистику кеша
     * GET /api/cache/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Статистика Caffeine (L1)
        stats.put("caffeine", cacheMonitoringConfig.getCaffeineStats());
        
        // Информация о Redis (L2)
        stats.put("redis", cacheMonitoringConfig.getRedisInfo());
        
        // Количество ключей в Redis
        stats.put("redisKeyCount", cacheMonitoringConfig.getRedisKeyCount());
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Получить только статистику Caffeine
     * GET /api/cache/stats/caffeine
     */
    @GetMapping("/stats/caffeine")
    public ResponseEntity<Map<String, Object>> getCaffeineStats() {
        return ResponseEntity.ok(cacheMonitoringConfig.getCaffeineStats());
    }

    /**
     * Получить только информацию о Redis
     * GET /api/cache/stats/redis
     */
    @GetMapping("/stats/redis")
    public ResponseEntity<Map<String, Object>> getRedisStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("info", cacheMonitoringConfig.getRedisInfo());
        stats.put("keyCount", cacheMonitoringConfig.getRedisKeyCount());
        return ResponseEntity.ok(stats);
    }
}