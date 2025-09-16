package com.example.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация для мониторинга кеша
 */
@Configuration
public class CacheMonitoringConfig {

    @Autowired
    @Qualifier("caffeineCacheManager")
    private CacheManager caffeineCacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Получить статистику Caffeine кеша
     */
    public Map<String, Object> getCaffeineStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Получаем статистику из Caffeine
            com.github.benmanes.caffeine.cache.Cache<Object, Object> cache = 
                ((com.github.benmanes.caffeine.cache.Cache<Object, Object>) 
                    caffeineCacheManager.getCache("users").getNativeCache());
            
            CacheStats cacheStats = cache.stats();
            
            stats.put("hitCount", cacheStats.hitCount());
            stats.put("missCount", cacheStats.missCount());
            stats.put("hitRate", cacheStats.hitRate());
            stats.put("evictionCount", cacheStats.evictionCount());
            stats.put("averageLoadPenalty", cacheStats.averageLoadPenalty());
            stats.put("requestCount", cacheStats.requestCount());
            
        } catch (Exception e) {
            stats.put("error", "Failed to get Caffeine stats: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * Получить информацию о Redis кеше
     */
    public Map<String, Object> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // Получаем информацию о Redis
            Map<String, String> redisInfo = redisTemplate.getConnectionFactory()
                .getConnection()
                .info("memory");
            
            info.put("usedMemory", redisInfo.get("used_memory_human"));
            info.put("maxMemory", redisInfo.get("maxmemory_human"));
            info.put("keyspace", redisInfo.get("db0"));
            
        } catch (Exception e) {
            info.put("error", "Failed to get Redis info: " + e.getMessage());
        }
        
        return info;
    }

    /**
     * Получить количество ключей в Redis
     */
    public long getRedisKeyCount() {
        try {
            return redisTemplate.getConnectionFactory()
                .getConnection()
                .dbSize();
        } catch (Exception e) {
            return -1;
        }
    }
}