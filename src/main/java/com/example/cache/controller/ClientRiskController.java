package com.example.cache.controller;

import com.example.cache.service.ClientRiskCacheService;
import kg.ssm.domain.redis.ClientRiskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для работы с ClientRiskDto
 * Демонстрирует интеграцию с существующим RedisService
 */
@RestController
@RequestMapping("/api/client-risk")
public class ClientRiskController {

    private static final Logger logger = LoggerFactory.getLogger(ClientRiskController.class);

    @Autowired
    private ClientRiskCacheService clientRiskCacheService;

    /**
     * Получить ClientRiskDto по ключу
     * GET /api/client-risk/{key}
     */
    @GetMapping("/{key}")
    public ResponseEntity<ClientRiskDto> getClientRisk(@PathVariable String key) {
        logger.info("Getting ClientRisk by key: {}", key);
        
        long startTime = System.currentTimeMillis();
        ClientRiskDto clientRisk = clientRiskCacheService.getClientRisk(key);
        long endTime = System.currentTimeMillis();
        
        logger.info("ClientRisk retrieval took {} ms", endTime - startTime);
        
        if (clientRisk != null) {
            return ResponseEntity.ok(clientRisk);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Сохранить ClientRiskDto
     * POST /api/client-risk
     */
    @PostMapping
    public ResponseEntity<String> setClientRisk(@RequestParam String key, @RequestBody ClientRiskDto clientRisk) {
        logger.info("Setting ClientRisk for key: {}", key);
        
        long startTime = System.currentTimeMillis();
        clientRiskCacheService.setClientRisk(key, clientRisk);
        long endTime = System.currentTimeMillis();
        
        logger.info("ClientRisk setting took {} ms", endTime - startTime);
        
        return ResponseEntity.ok("ClientRisk cached successfully");
    }

    /**
     * Удалить ClientRiskDto
     * DELETE /api/client-risk/{key}
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<String> deleteClientRisk(@PathVariable String key) {
        logger.info("Deleting ClientRisk for key: {}", key);
        
        long startTime = System.currentTimeMillis();
        clientRiskCacheService.deleteClientRisk(key);
        long endTime = System.currentTimeMillis();
        
        logger.info("ClientRisk deletion took {} ms", endTime - startTime);
        
        return ResponseEntity.ok("ClientRisk deleted successfully");
    }

    /**
     * Получить список ошибок
     * GET /api/client-risk/errors
     */
    @GetMapping("/errors")
    public ResponseEntity<List<ClientRiskDto>> getErrorValues() {
        logger.info("Getting error values");
        
        long startTime = System.currentTimeMillis();
        List<ClientRiskDto> errors = clientRiskCacheService.getErrorValues();
        long endTime = System.currentTimeMillis();
        
        logger.info("Error values retrieval took {} ms", endTime - startTime);
        
        return ResponseEntity.ok(errors);
    }

    /**
     * Очистить кеш ClientRisk
     * POST /api/client-risk/cache/clear
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<String> clearClientRiskCache() {
        logger.info("Clearing ClientRisk cache");
        
        long startTime = System.currentTimeMillis();
        clientRiskCacheService.clearClientRiskCache();
        long endTime = System.currentTimeMillis();
        
        logger.info("Cache clearing took {} ms", endTime - startTime);
        
        return ResponseEntity.ok("ClientRisk cache cleared successfully");
    }

    /**
     * Получить статистику кеша
     * GET /api/client-risk/cache/stats
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<String> getCacheStats() {
        logger.info("Getting cache stats");
        
        String stats = clientRiskCacheService.getCacheStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Проверить доступность Redis
     * GET /api/client-risk/cache/health
     */
    @GetMapping("/cache/health")
    public ResponseEntity<String> getCacheHealth() {
        boolean redisAvailable = clientRiskCacheService.isRedisAvailable();
        
        if (redisAvailable) {
            return ResponseEntity.ok("Redis is available");
        } else {
            return ResponseEntity.status(503).body("Redis is not available");
        }
    }
}