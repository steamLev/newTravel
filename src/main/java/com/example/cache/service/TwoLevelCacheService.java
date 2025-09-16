package com.example.cache.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Сервис для работы с двухуровневым кешем
 * L1 - Caffeine (локальный кеш)
 * L2 - Redis (распределенный кеш)
 */
@Service
public class TwoLevelCacheService {

    private static final Logger logger = LoggerFactory.getLogger(TwoLevelCacheService.class);

    @Autowired
    @Qualifier("caffeineCacheManager")
    private CacheManager l1CacheManager; // Caffeine

    @Autowired
    @Qualifier("redisCacheManager")
    private CacheManager l2CacheManager; // Redis

    /**
     * Получить значение из кеша с двухуровневой стратегией
     * 1. Сначала проверяем L1 (Caffeine)
     * 2. Если нет в L1, проверяем L2 (Redis)
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

            // Попытка получить из L2 (Redis)
            value = getFromL2(cacheName, key, type);
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
                putToL2(cacheName, key, value);
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
            putToL2(cacheName, key, value);
            logger.debug("Value cached in L1 and L2: {}:{}", cacheName, key);
        }
    }

    /**
     * Удалить значение из обоих кешей
     */
    public void evict(String cacheName, String key) {
        evictFromL1(cacheName, key);
        evictFromL2(cacheName, key);
        logger.debug("Value evicted from L1 and L2: {}:{}", cacheName, key);
    }

    /**
     * Очистить весь кеш
     */
    public void clear(String cacheName) {
        clearL1(cacheName);
        clearL2(cacheName);
        logger.debug("Cache cleared: {}", cacheName);
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

    // L2 Cache (Redis) operations
    private <T> T getFromL2(String cacheName, String key, Class<T> type) {
        try {
            Cache cache = l2CacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    return type.cast(wrapper.get());
                }
            }
        } catch (Exception e) {
            logger.warn("Error getting from L2 cache: {}:{}", cacheName, key, e);
        }
        return null;
    }

    private void putToL2(String cacheName, String key, Object value) {
        try {
            Cache cache = l2CacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
            }
        } catch (Exception e) {
            logger.warn("Error putting to L2 cache: {}:{}", cacheName, key, e);
        }
    }

    private void evictFromL2(String cacheName, String key) {
        try {
            Cache cache = l2CacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
            }
        } catch (Exception e) {
            logger.warn("Error evicting from L2 cache: {}:{}", cacheName, key, e);
        }
    }

    private void clearL2(String cacheName) {
        try {
            Cache cache = l2CacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        } catch (Exception e) {
            logger.warn("Error clearing L2 cache: {}", cacheName, e);
        }
    }
}