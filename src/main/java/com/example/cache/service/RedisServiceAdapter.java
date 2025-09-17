package com.example.cache.service;

import kg.ssm.domain.redis.ClientRiskDto;
import kg.ssm.service.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Адаптер для интеграции существующего RedisService с двухуровневым кешем
 * Обеспечивает совместимость с существующей логикой работы с Redis
 */
@Service
public class RedisServiceAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RedisServiceAdapter.class);

    @Autowired
    private RedisService redisService;

    /**
     * Получить значение из Redis через существующий RedisService
     */
    public <T> T get(String key, Class<T> type) {
        try {
            if (type == ClientRiskDto.class) {
                return type.cast(redisService.getValue(key));
            }
            // Для других типов можно добавить дополнительную логику
            logger.warn("Unsupported type for RedisService: {}", type.getSimpleName());
            return null;
        } catch (Exception e) {
            logger.warn("Error getting value from Redis: {}", key, e);
            return null;
        }
    }

    /**
     * Сохранить значение в Redis через существующий RedisService
     */
    public void put(String key, Object value) {
        try {
            if (value instanceof ClientRiskDto) {
                redisService.setValue(key, value);
            } else {
                logger.warn("Unsupported value type for RedisService: {}", 
                    value != null ? value.getClass().getSimpleName() : "null");
            }
        } catch (Exception e) {
            logger.warn("Error putting value to Redis: {}", key, e);
        }
    }

    /**
     * Удалить значение из Redis через существующий RedisService
     */
    public void delete(String key) {
        try {
            redisService.deleteValue(key);
        } catch (Exception e) {
            logger.warn("Error deleting value from Redis: {}", key, e);
        }
    }

    /**
     * Получить список ошибок через существующий RedisService
     */
    public List<ClientRiskDto> getErrorValues() {
        try {
            return redisService.getErrorValue();
        } catch (Exception e) {
            logger.warn("Error getting error values from Redis", e);
            return List.of();
        }
    }

    /**
     * Проверить доступность Redis
     */
    public boolean isAvailable() {
        try {
            // Простая проверка доступности через попытку получения несуществующего ключа
            redisService.getValue("health_check_" + System.currentTimeMillis());
            return true;
        } catch (Exception e) {
            logger.debug("Redis is not available", e);
            return false;
        }
    }
}