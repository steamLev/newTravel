package com.example.cache.service;

import kg.ssm.domain.redis.ClientRiskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Сервис для работы с ClientRiskDto с использованием интегрированного двухуровневого кеша
 * Совместим с существующим RedisService
 */
@Service
public class ClientRiskCacheService {

    private static final Logger logger = LoggerFactory.getLogger(ClientRiskCacheService.class);
    private static final String CACHE_NAME = "client_risk";

    @Autowired
    private IntegratedTwoLevelCacheService cacheService;

    /**
     * Получить ClientRiskDto по ключу с кешированием
     * Совместимо с существующим RedisService.getValue()
     */
    public ClientRiskDto getClientRisk(String key) {
        return cacheService.get(CACHE_NAME, key, ClientRiskDto.class, () -> {
            logger.info("Loading ClientRisk from source: {}", key);
            // Здесь должна быть ваша логика загрузки из источника данных
            // Например, из базы данных или внешнего API
            return loadClientRiskFromSource(key);
        });
    }

    /**
     * Сохранить ClientRiskDto в кеш
     * Совместимо с существующим RedisService.setValue()
     */
    public void setClientRisk(String key, ClientRiskDto value) {
        logger.info("Caching ClientRisk: {}", key);
        cacheService.put(CACHE_NAME, key, value);
    }

    /**
     * Удалить ClientRiskDto из кеша
     * Совместимо с существующим RedisService.deleteValue()
     */
    public void deleteClientRisk(String key) {
        logger.info("Deleting ClientRisk from cache: {}", key);
        cacheService.evict(CACHE_NAME, key);
    }

    /**
     * Получить список ошибок
     * Совместимо с существующим RedisService.getErrorValue()
     */
    public List<ClientRiskDto> getErrorValues() {
        logger.info("Getting error values from cache");
        return cacheService.getErrorValues();
    }

    /**
     * Получить ClientRiskDto с кастомной логикой загрузки
     */
    public ClientRiskDto getClientRisk(String key, Callable<ClientRiskDto> valueLoader) {
        return cacheService.get(CACHE_NAME, key, ClientRiskDto.class, valueLoader);
    }

    /**
     * Получить ClientRiskDto с функцией загрузки
     */
    public ClientRiskDto getClientRisk(String key, java.util.function.Function<String, ClientRiskDto> valueLoader) {
        return cacheService.get(CACHE_NAME, key, ClientRiskDto.class, () -> valueLoader.apply(key));
    }

    /**
     * Очистить кеш ClientRisk
     */
    public void clearClientRiskCache() {
        logger.info("Clearing ClientRisk cache");
        cacheService.clear(CACHE_NAME);
    }

    /**
     * Проверить доступность Redis
     */
    public boolean isRedisAvailable() {
        return cacheService.isRedisAvailable();
    }

    /**
     * Получить статистику кеша
     */
    public String getCacheStats() {
        return String.format("Redis available: %s, Cache: %s", 
            isRedisAvailable(), CACHE_NAME);
    }

    /**
     * Загрузка ClientRiskDto из источника данных
     * Заглушка - замените на вашу реальную логику
     */
    private ClientRiskDto loadClientRiskFromSource(String key) {
        // Здесь должна быть ваша логика загрузки из источника данных
        // Например:
        // - Запрос к базе данных
        // - Вызов внешнего API
        // - Обработка файла
        
        logger.debug("Loading ClientRisk from source for key: {}", key);
        
        // Заглушка - создаем пустой объект
        ClientRiskDto clientRisk = new ClientRiskDto();
        // Установите необходимые поля
        // clientRisk.setPartyId(key);
        // clientRisk.setError("Not found");
        
        return clientRisk;
    }
}