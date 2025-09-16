package com.example.cache.service;

import com.example.cache.annotation.CacheRoute;
import kg.ssm.domain.redis.ClientRiskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Сервис маршрутизации кешей
 * Позволяет гибко управлять тем, какие данные в какие кеши отправляются
 */
@Service
public class CacheRoutingService {

    private static final Logger logger = LoggerFactory.getLogger(CacheRoutingService.class);

    @Autowired
    @Qualifier("caffeineCacheManager")
    private CacheManager l1CacheManager; // Caffeine

    @Autowired
    private RedisServiceAdapter redisServiceAdapter; // Redis через ваш RedisService

    /**
     * Получить значение с маршрутизацией кешей
     */
    public <T> T get(String key, Class<T> type, CacheRoute.CacheType[] cacheTypes, Callable<T> valueLoader) {
        return get("default", key, type, cacheTypes, valueLoader);
    }

    /**
     * Получить значение с маршрутизацией кешей и именем кеша
     */
    public <T> T get(String cacheName, String key, Class<T> type, CacheRoute.CacheType[] cacheTypes, Callable<T> valueLoader) {
        try {
            // Проверяем кеши в порядке приоритета
            for (CacheRoute.CacheType cacheType : cacheTypes) {
                T value = getFromCache(cacheName, key, type, cacheType);
                if (value != null) {
                    logger.debug("Cache HIT {}: {}:{}", cacheType, cacheName, key);
                    return value;
                }
            }

            // Значение не найдено в кешах, загружаем
            logger.debug("Cache MISS: {}:{}", cacheName, key);
            T value = valueLoader.call();
            
            if (value != null) {
                // Сохраняем во все указанные кеши
                for (CacheRoute.CacheType cacheType : cacheTypes) {
                    putToCache(cacheName, key, value, cacheType);
                }
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
     * Получить значение с функцией загрузки
     */
    public <T> T get(String cacheName, String key, Class<T> type, CacheRoute.CacheType[] cacheTypes, Function<String, T> valueLoader) {
        return get(cacheName, key, type, cacheTypes, () -> valueLoader.apply(key));
    }

    /**
     * Сохранить значение в указанные кеши
     */
    public void put(String key, Object value, CacheRoute.CacheType[] cacheTypes) {
        put("default", key, value, cacheTypes);
    }

    /**
     * Сохранить значение в указанные кеши с именем кеша
     */
    public void put(String cacheName, String key, Object value, CacheRoute.CacheType[] cacheTypes) {
        if (value != null) {
            for (CacheRoute.CacheType cacheType : cacheTypes) {
                putToCache(cacheName, key, value, cacheType);
            }
            logger.debug("Value cached in {}: {}:{}", Arrays.toString(cacheTypes), cacheName, key);
        }
    }

    /**
     * Удалить значение из указанных кешей
     */
    public void evict(String key, CacheRoute.CacheType[] cacheTypes) {
        evict("default", key, cacheTypes);
    }

    /**
     * Удалить значение из указанных кешей с именем кеша
     */
    public void evict(String cacheName, String key, CacheRoute.CacheType[] cacheTypes) {
        for (CacheRoute.CacheType cacheType : cacheTypes) {
            evictFromCache(cacheName, key, cacheType);
        }
        logger.debug("Value evicted from {}: {}:{}", Arrays.toString(cacheTypes), cacheName, key);
    }

    /**
     * Очистить указанные кеши
     */
    public void clear(CacheRoute.CacheType[] cacheTypes) {
        clear("default", cacheTypes);
    }

    /**
     * Очистить указанные кеши с именем кеша
     */
    public void clear(String cacheName, CacheRoute.CacheType[] cacheTypes) {
        for (CacheRoute.CacheType cacheType : cacheTypes) {
            clearCache(cacheName, cacheType);
        }
        logger.debug("Cache cleared: {} in {}", cacheName, Arrays.toString(cacheTypes));
    }

    // Специальные методы для AmlClient (только Redis)
    public ClientRiskDto getAmlClient(String key) {
        return get("aml_client", key, ClientRiskDto.class, 
            new CacheRoute.CacheType[]{CacheRoute.CacheType.L2}, 
            () -> {
                logger.info("Loading AmlClient from source: {}", key);
                return loadAmlClientFromSource(key);
            });
    }

    public void setAmlClient(String key, ClientRiskDto value) {
        put("aml_client", key, value, new CacheRoute.CacheType[]{CacheRoute.CacheType.L2});
    }

    public void deleteAmlClient(String key) {
        evict("aml_client", key, new CacheRoute.CacheType[]{CacheRoute.CacheType.L2});
    }

    public List<ClientRiskDto> getAmlClientErrors() {
        return redisServiceAdapter.getErrorValues();
    }

    // Специальные методы для обычных данных (двухуровневый кеш)
    public <T> T getWithTwoLevelCache(String cacheName, String key, Class<T> type, Callable<T> valueLoader) {
        return get(cacheName, key, type, 
            new CacheRoute.CacheType[]{CacheRoute.CacheType.L1, CacheRoute.CacheType.L2}, 
            valueLoader);
    }

    public void putWithTwoLevelCache(String cacheName, String key, Object value) {
        put(cacheName, key, value, 
            new CacheRoute.CacheType[]{CacheRoute.CacheType.L1, CacheRoute.CacheType.L2});
    }

    // Специальные методы для быстрых данных (только Caffeine)
    public <T> T getWithCaffeineOnly(String cacheName, String key, Class<T> type, Callable<T> valueLoader) {
        return get(cacheName, key, type, 
            new CacheRoute.CacheType[]{CacheRoute.CacheType.L1}, 
            valueLoader);
    }

    public void putWithCaffeineOnly(String cacheName, String key, Object value) {
        put(cacheName, key, value, 
            new CacheRoute.CacheType[]{CacheRoute.CacheType.L1});
    }

    // Вспомогательные методы для работы с кешами
    private <T> T getFromCache(String cacheName, String key, Class<T> type, CacheRoute.CacheType cacheType) {
        try {
            switch (cacheType) {
                case L1:
                    return getFromL1(cacheName, key, type);
                case L2:
                    return getFromL2(key, type);
                default:
                    return null;
            }
        } catch (Exception e) {
            logger.warn("Error getting from {} cache: {}:{}", cacheType, cacheName, key, e);
            return null;
        }
    }

    private void putToCache(String cacheName, String key, Object value, CacheRoute.CacheType cacheType) {
        try {
            switch (cacheType) {
                case L1:
                    putToL1(cacheName, key, value);
                    break;
                case L2:
                    putToL2(key, value);
                    break;
            }
        } catch (Exception e) {
            logger.warn("Error putting to {} cache: {}:{}", cacheType, cacheName, key, e);
        }
    }

    private void evictFromCache(String cacheName, String key, CacheRoute.CacheType cacheType) {
        try {
            switch (cacheType) {
                case L1:
                    evictFromL1(cacheName, key);
                    break;
                case L2:
                    evictFromL2(key);
                    break;
            }
        } catch (Exception e) {
            logger.warn("Error evicting from {} cache: {}:{}", cacheType, cacheName, key, e);
        }
    }

    private void clearCache(String cacheName, CacheRoute.CacheType cacheType) {
        try {
            switch (cacheType) {
                case L1:
                    clearL1(cacheName);
                    break;
                case L2:
                    // Для Redis не очищаем полностью, так как это может повлиять на другие данные
                    logger.warn("Full Redis cache clear not implemented for safety");
                    break;
            }
        } catch (Exception e) {
            logger.warn("Error clearing {} cache: {}", cacheType, cacheName, e);
        }
    }

    // L1 Cache (Caffeine) operations
    private <T> T getFromL1(String cacheName, String key, Class<T> type) {
        Cache cache = l1CacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                return type.cast(wrapper.get());
            }
        }
        return null;
    }

    private void putToL1(String cacheName, String key, Object value) {
        Cache cache = l1CacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    private void evictFromL1(String cacheName, String key) {
        Cache cache = l1CacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    private void clearL1(String cacheName) {
        Cache cache = l1CacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    // L2 Cache (Redis) operations
    private <T> T getFromL2(String key, Class<T> type) {
        return redisServiceAdapter.get(key, type);
    }

    private void putToL2(String key, Object value) {
        redisServiceAdapter.put(key, value);
    }

    private void evictFromL2(String key) {
        redisServiceAdapter.delete(key);
    }

    // Заглушка для загрузки AmlClient из источника
    private ClientRiskDto loadAmlClientFromSource(String key) {
        // Здесь должна быть ваша логика загрузки AmlClient из источника данных
        logger.debug("Loading AmlClient from source for key: {}", key);
        
        // Заглушка
        ClientRiskDto clientRisk = new ClientRiskDto();
        // Установите необходимые поля
        // clientRisk.setPartyId(key);
        // clientRisk.setError("Loaded from source");
        
        return clientRisk;
    }
}