package com.example.cache.service;

import kg.ssm.domain.redis.ClientRiskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Интегрированный двухуровневый кеш сервис
 * L1 - Caffeine (локальный кеш)
 * L2 - Существующий RedisService (распределенный кеш)
 */
@Service
public class IntegratedTwoLevelCacheService {

    private static final Logger logger = LoggerFactory.getLogger(IntegratedTwoLevelCacheService.class);

    @Autowired
    @Qualifier("caffeineCacheManager")
    private CacheManager l1CacheManager; // Caffeine

    @Autowired
    private RedisServiceAdapter redisServiceAdapter; // Адаптер для существующего RedisService

    /**
     * Получить значение из кеша с двухуровневой стратегией
     * 1. Сначала проверяем L1 (Caffeine)
     * 2. Если нет в L1, проверяем L2 (RedisService)
     * 3. Если нет в L2, выполняем функцию и сохраняем в оба кеша
     */
    public <T> T get(String cacheName, String key, Class<T> type, Callable<T> valueLoader) {
        try {
            // Попытка получить из L1 (Caffeine)
            T value = getFromL1(cacheName, key, type);
            if (value != null) {
                logger.debug("Cache HIT L1: {}:{}", cacheName, key);
                return value;
            }

            // Попытка получить из L2 (RedisService)
            value = getFromL2(key, type);
            if (value != null) {
                logger.debug("Cache HIT L2: {}:{}", cacheName, key);
                // Сохраняем в L1 для будущих запросов
                putToL1(cacheName, key, value);
                return value;
            }

            // Значение не найдено в кешах, загружаем и сохраняем
            logger.debug("Cache MISS: {}:{}", cacheName, key);
            value = valueLoader.call();
            if (value != null) {
                putToL1(cacheName, key, value);
                putToL2(key, value);
            }
            return value;

        } catch (Exception e) {
            logger.error("Error getting value from cache: {}:{}", cacheName, key, e);
            try {
                return valueLoader.call();
            } catch (Exception ex) {
                logger.error("Error in value loader", ex);
                throw new RuntimeException("Failed to load value", ex);
            }
        }
    }

    /**
     * Получить значение из кеша с функцией загрузки
     */
    public <T> T get(String cacheName, String key, Class<T> type, Function<String, T> valueLoader) {
        return get(cacheName, key, type, () -> valueLoader.apply(key));
    }

    /**
     * Сохранить значение в оба кеша
     */
    public void put(String cacheName, String key, Object value) {
        if (value != null) {
            putToL1(cacheName, key, value);
            putToL2(key, value);
            logger.debug("Value cached in L1 and L2: {}:{}", cacheName, key);
        }
    }

    /**
     * Удалить значение из обоих кешей
     */
    public void evict(String cacheName, String key) {
        evictFromL1(cacheName, key);
        evictFromL2(key);
        logger.debug("Value evicted from L1 and L2: {}:{}", cacheName, key);
    }

    /**
     * Очистить весь кеш
     */
    public void clear(String cacheName) {
        clearL1(cacheName);
        // Для RedisService не реализуем полную очистку, так как это может повлиять на другие данные
        logger.debug("Cache cleared: {}", cacheName);
    }

    /**
     * Специальный метод для работы с ClientRiskDto через существующий RedisService
     */
    public ClientRiskDto getClientRisk(String key) {
        return get("client_risk", key, ClientRiskDto.class, () -> {
            logger.info("Loading ClientRisk from source: {}", key);
            // Здесь должна быть логика загрузки из источника данных
            return null; // Заглушка
        });
    }

    /**
     * Сохранить ClientRiskDto
     */
    public void putClientRisk(String key, ClientRiskDto value) {
        put("client_risk", key, value);
    }

    /**
     * Получить список ошибок через существующий RedisService
     */
    public List<ClientRiskDto> getErrorValues() {
        try {
            return redisServiceAdapter.getErrorValues();
        } catch (Exception e) {
            logger.error("Error getting error values", e);
            return List.of();
        }
    }

    /**
     * Проверить доступность Redis
     */
    public boolean isRedisAvailable() {
        return redisServiceAdapter.isAvailable();
    }

    // L1 Cache (Caffeine) operations
    private <T> T getFromL1(String cacheName, String key, Class<T> type) {
        try {
            Cache cache = l1CacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    return type.cast(wrapper.get());
                }
            }
        } catch (Exception e) {
            logger.warn("Error getting from L1 cache: {}:{}", cacheName, key, e);
        }
        return null;
    }

    private void putToL1(String cacheName, String key, Object value) {
        try {
            Cache cache = l1CacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
            }
        } catch (Exception e) {
            logger.warn("Error putting to L1 cache: {}:{}", cacheName, key, e);
        }
    }

    private void evictFromL1(String cacheName, String key) {
        try {
            Cache cache = l1CacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
            }
        } catch (Exception e) {
            logger.warn("Error evicting from L1 cache: {}:{}", cacheName, key, e);
        }
    }

    private void clearL1(String cacheName) {
        try {
            Cache cache = l1CacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        } catch (Exception e) {
            logger.warn("Error clearing L1 cache: {}", cacheName, e);
        }
    }

    // L2 Cache (RedisService) operations
    private <T> T getFromL2(String key, Class<T> type) {
        try {
            return redisServiceAdapter.get(key, type);
        } catch (Exception e) {
            logger.warn("Error getting from L2 cache: {}", key, e);
        }
        return null;
    }

    private void putToL2(String key, Object value) {
        try {
            redisServiceAdapter.put(key, value);
        } catch (Exception e) {
            logger.warn("Error putting to L2 cache: {}", key, e);
        }
    }

    private void evictFromL2(String key) {
        try {
            redisServiceAdapter.delete(key);
        } catch (Exception e) {
            logger.warn("Error evicting from L2 cache: {}", key, e);
        }
    }
}