package com.example.cache.controller;

import com.example.cache.service.RoutedAmlService;
import kg.ssm.domain.redis.ClientRiskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для демонстрации маршрутизации кешей
 * Показывает как разные типы данных маршрутизируются в разные кеши
 */
@RestController
@RequestMapping("/api/routed-cache")
public class RoutedCacheController {

    private static final Logger logger = LoggerFactory.getLogger(RoutedCacheController.class);

    @Autowired
    private RoutedAmlService routedAmlService;

    // ========== AmlClient - ТОЛЬКО Redis (L2) ==========

    /**
     * Получить AmlClient (только Redis)
     * GET /api/routed-cache/aml-client/{partyId}
     */
    @GetMapping("/aml-client/{partyId}")
    public ResponseEntity<ClientRiskDto> getAmlClient(@PathVariable String partyId) {
        logger.info("Getting AmlClient (Redis only): {}", partyId);
        
        long startTime = System.currentTimeMillis();
        ClientRiskDto clientRisk = routedAmlService.getAmlClient(partyId);
        long endTime = System.currentTimeMillis();
        
        logger.info("AmlClient retrieval took {} ms", endTime - startTime);
        
        if (clientRisk != null) {
            return ResponseEntity.ok(clientRisk);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Сохранить AmlClient (только Redis)
     * POST /api/routed-cache/aml-client/{partyId}
     */
    @PostMapping("/aml-client/{partyId}")
    public ResponseEntity<String> setAmlClient(@PathVariable String partyId, @RequestBody ClientRiskDto clientRisk) {
        logger.info("Setting AmlClient (Redis only): {}", partyId);
        
        long startTime = System.currentTimeMillis();
        routedAmlService.setAmlClient(partyId, clientRisk);
        long endTime = System.currentTimeMillis();
        
        logger.info("AmlClient setting took {} ms", endTime - startTime);
        
        return ResponseEntity.ok("AmlClient cached in Redis only");
    }

    /**
     * Получить ошибки AmlClient (только Redis)
     * GET /api/routed-cache/aml-client/errors
     */
    @GetMapping("/aml-client/errors")
    public ResponseEntity<List<ClientRiskDto>> getAmlClientErrors() {
        logger.info("Getting AmlClient errors (Redis only)");
        
        long startTime = System.currentTimeMillis();
        List<ClientRiskDto> errors = routedAmlService.getAmlClientErrors();
        long endTime = System.currentTimeMillis();
        
        logger.info("AmlClient errors retrieval took {} ms", endTime - startTime);
        
        return ResponseEntity.ok(errors);
    }

    // ========== Пользовательские данные - ДВУХУРОВНЕВЫЙ кеш ==========

    /**
     * Получить пользовательские данные (L1 + L2)
     * GET /api/routed-cache/user-data/{userId}
     */
    @GetMapping("/user-data/{userId}")
    public ResponseEntity<String> getUserData(@PathVariable String userId) {
        logger.info("Getting user data (Two-level cache): {}", userId);
        
        long startTime = System.currentTimeMillis();
        String userData = routedAmlService.getUserData(userId);
        long endTime = System.currentTimeMillis();
        
        logger.info("User data retrieval took {} ms", endTime - startTime);
        
        return ResponseEntity.ok(userData);
    }

    /**
     * Сохранить пользовательские данные (L1 + L2)
     * POST /api/routed-cache/user-data/{userId}
     */
    @PostMapping("/user-data/{userId}")
    public ResponseEntity<String> setUserData(@PathVariable String userId, @RequestBody String userData) {
        logger.info("Setting user data (Two-level cache): {}", userId);
        
        long startTime = System.currentTimeMillis();
        routedAmlService.setUserData(userId, userData);
        long endTime = System.currentTimeMillis();
        
        logger.info("User data setting took {} ms", endTime - startTime);
        
        return ResponseEntity.ok("User data cached in L1 and L2");
    }

    // ========== Временные данные - ТОЛЬКО Caffeine (L1) ==========

    /**
     * Получить временные данные (только Caffeine)
     * GET /api/routed-cache/temp-data/{key}
     */
    @GetMapping("/temp-data/{key}")
    public ResponseEntity<String> getTempData(@PathVariable String key) {
        logger.info("Getting temp data (Caffeine only): {}", key);
        
        long startTime = System.currentTimeMillis();
        String tempData = routedAmlService.getTempData(key);
        long endTime = System.currentTimeMillis();
        
        logger.info("Temp data retrieval took {} ms", endTime - startTime);
        
        return ResponseEntity.ok(tempData);
    }

    /**
     * Сохранить временные данные (только Caffeine)
     * POST /api/routed-cache/temp-data/{key}
     */
    @PostMapping("/temp-data/{key}")
    public ResponseEntity<String> setTempData(@PathVariable String key, @RequestBody String data) {
        logger.info("Setting temp data (Caffeine only): {}", key);
        
        long startTime = System.currentTimeMillis();
        routedAmlService.setTempData(key, data);
        long endTime = System.currentTimeMillis();
        
        logger.info("Temp data setting took {} ms", endTime - startTime);
        
        return ResponseEntity.ok("Temp data cached in Caffeine only");
    }

    // ========== Конфигурационные данные - ТОЛЬКО Caffeine (L1) ==========

    /**
     * Получить конфигурационные данные (только Caffeine)
     * GET /api/routed-cache/config-data/{configKey}
     */
    @GetMapping("/config-data/{configKey}")
    public ResponseEntity<String> getConfigData(@PathVariable String configKey) {
        logger.info("Getting config data (Caffeine only): {}", configKey);
        
        long startTime = System.currentTimeMillis();
        String configData = routedAmlService.getConfigData(configKey);
        long endTime = System.currentTimeMillis();
        
        logger.info("Config data retrieval took {} ms", endTime - startTime);
        
        return ResponseEntity.ok(configData);
    }

    // ========== Статистика - ДВУХУРОВНЕВЫЙ кеш ==========

    /**
     * Получить статистику (L1 + L2)
     * GET /api/routed-cache/statistics/{statKey}
     */
    @GetMapping("/statistics/{statKey}")
    public ResponseEntity<String> getStatistics(@PathVariable String statKey) {
        logger.info("Getting statistics (Two-level cache): {}", statKey);
        
        long startTime = System.currentTimeMillis();
        String statistics = routedAmlService.getStatistics(statKey);
        long endTime = System.currentTimeMillis();
        
        logger.info("Statistics retrieval took {} ms", endTime - startTime);
        
        return ResponseEntity.ok(statistics);
    }

    // ========== Управление кешами ==========

    /**
     * Очистить кеши
     * POST /api/routed-cache/clear
     */
    @PostMapping("/clear")
    public ResponseEntity<String> clearCaches() {
        logger.info("Clearing caches");
        
        long startTime = System.currentTimeMillis();
        routedAmlService.clearCaches();
        long endTime = System.currentTimeMillis();
        
        logger.info("Cache clearing took {} ms", endTime - startTime);
        
        return ResponseEntity.ok("Caches cleared successfully");
    }

    /**
     * Обработать ошибки AmlClient
     * POST /api/routed-cache/aml-client/process-errors
     */
    @PostMapping("/aml-client/process-errors")
    public ResponseEntity<String> processAmlClientErrors() {
        logger.info("Processing AmlClient errors");
        
        long startTime = System.currentTimeMillis();
        routedAmlService.processAmlClientErrors();
        long endTime = System.currentTimeMillis();
        
        logger.info("Error processing took {} ms", endTime - startTime);
        
        return ResponseEntity.ok("AmlClient errors processed successfully");
    }

    /**
     * Получить информацию о маршрутизации
     * GET /api/routed-cache/info
     */
    @GetMapping("/info")
    public ResponseEntity<String> getRoutingInfo() {
        String info = """
            Cache Routing Information:
            
            AmlClient Data:
            - Route: Redis only (L2)
            - Reason: Shared between instances, persistent
            - TTL: 3600 seconds
            
            User Data:
            - Route: Two-level cache (L1 + L2)
            - Reason: Frequently accessed, needs speed
            - L1 TTL: 300 seconds, L2 TTL: 3600 seconds
            
            Temp Data:
            - Route: Caffeine only (L1)
            - Reason: Temporary, instance-specific
            - TTL: 60 seconds
            
            Config Data:
            - Route: Caffeine only (L1)
            - Reason: Rarely changes, needs speed
            - TTL: 1800 seconds
            
            Statistics:
            - Route: Two-level cache (L1 + L2)
            - Reason: Shared data, needs speed
            - L1 TTL: 60 seconds, L2 TTL: 1800 seconds
            """;
        
        return ResponseEntity.ok(info);
    }
}