package com.example.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * Конфигурация интегрированного двухуровневого кеша
 * Совместима с существующим RedisService
 */
@Configuration
@EnableCaching
public class IntegratedCacheConfig {

    @Value("${cache.caffeine.spec:maximumSize=1000,expireAfterWrite=5m,recordStats}")
    private String caffeineSpec;

    @Value("${ttl.redis:3600}")
    private Integer redisTtl;

    /**
     * Конфигурация Caffeine кеша (L1 - локальный кеш)
     * Оптимизирована для работы с ClientRiskDto
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Специальная конфигурация для ClientRiskDto
        Caffeine<Object, Object> caffeine = Caffeine.from(caffeineSpec)
                .recordStats()
                .removalListener((key, value, cause) -> {
                    // Логирование удаления из кеша
                    System.out.println("Removed from L1 cache: " + key + " cause: " + cause);
                });

        cacheManager.setCaffeine(caffeine);
        
        // Предустановленные кеши
        cacheManager.setCacheNames("client_risk", "users", "products", "errors");
        
        return cacheManager;
    }

    /**
     * Конфигурация для работы с существующим RedisService
     * Настройки совместимости
     */
    @Bean
    public CacheConfigurationProperties cacheConfigurationProperties() {
        CacheConfigurationProperties props = new CacheConfigurationProperties();
        props.setRedisTtl(redisTtl);
        props.setKeyPrefix("ClientAml "); // Используем тот же префикс что и в RedisService
        props.setErrorPrefix("Error");
        return props;
    }

    /**
     * Свойства конфигурации кеша
     */
    public static class CacheConfigurationProperties {
        private Integer redisTtl;
        private String keyPrefix;
        private String errorPrefix;

        // Getters and Setters
        public Integer getRedisTtl() {
            return redisTtl;
        }

        public void setRedisTtl(Integer redisTtl) {
            this.redisTtl = redisTtl;
        }

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        public String getErrorPrefix() {
            return errorPrefix;
        }

        public void setErrorPrefix(String errorPrefix) {
            this.errorPrefix = errorPrefix;
        }
    }
}