package com.example.cache.service;

import com.example.cache.annotation.CacheRoute;
import com.example.cache.annotation.RedisOnly;
import com.example.cache.annotation.CaffeineOnly;
import com.example.cache.annotation.TwoLevelCache;
import kg.ssm.domain.redis.ClientRiskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Пример сервиса с маршрутизацией кешей
 * Показывает как разные типы данных маршрутизируются в разные кеши
 */
@Service
public class RoutedAmlService {

    private static final Logger logger = LoggerFactory.getLogger(RoutedAmlService.class);

    @Autowired
    private CacheRoutingService cacheRoutingService;

    // Константы из вашего RedisService
    private static final String ERROR_FATAL = "Фатальная ошибка при сохранении в АБС:";
    private static final String ERROR_SYNCHRONIZE = "Ошибка при синхронизации клиента subjectId:";
    private static final String ERROR_NOT_RESPONSE = "Нет ответа от ABS";
    private static final String NOT_SUBJECT = "Сущность 'Субъект' по указанному PartyId";
    private static final String KEY_PREFIX = "ClientAml ";
    private static final String ERROR_PREFIX = "Error";

    /**
     * AmlClient - ТОЛЬКО в Redis (L2)
     * Использует @RedisOnly аннотацию
     */
    @RedisOnly(cacheName = "aml_client", ttl = 3600)
    public ClientRiskDto getAmlClient(String partyId) {
        String key = KEY_PREFIX + partyId;
        
        return cacheRoutingService.getAmlClient(key);
    }

    /**
     * Сохранить AmlClient - ТОЛЬКО в Redis (L2)
     */
    @RedisOnly(cacheName = "aml_client", ttl = 3600)
    public void setAmlClient(String partyId, ClientRiskDto clientRisk) {
        String key = KEY_PREFIX + partyId;
        cacheRoutingService.setAmlClient(key, clientRisk);
    }

    /**
     * Удалить AmlClient - ТОЛЬКО из Redis (L2)
     */
    @RedisOnly(cacheName = "aml_client")
    public void deleteAmlClient(String partyId) {
        String key = KEY_PREFIX + partyId;
        cacheRoutingService.deleteAmlClient(key);
    }

    /**
     * Получить ошибки AmlClient - ТОЛЬКО из Redis (L2)
     */
    @RedisOnly(cacheName = "aml_client")
    public List<ClientRiskDto> getAmlClientErrors() {
        return cacheRoutingService.getAmlClientErrors();
    }

    /**
     * Пользовательские данные - ДВУХУРОВНЕВЫЙ кеш (L1 + L2)
     * Использует @TwoLevelCache аннотацию
     */
    @TwoLevelCache(cacheName = "user_data", l1Ttl = 300, l2Ttl = 3600)
    public String getUserData(String userId) {
        return cacheRoutingService.getWithTwoLevelCache("user_data", userId, String.class, () -> {
            logger.info("Loading user data from source: {}", userId);
            return loadUserDataFromSource(userId);
        });
    }

    /**
     * Сохранить пользовательские данные - ДВУХУРОВНЕВЫЙ кеш
     */
    @TwoLevelCache(cacheName = "user_data")
    public void setUserData(String userId, String userData) {
        cacheRoutingService.putWithTwoLevelCache("user_data", userId, userData);
    }

    /**
     * Временные данные - ТОЛЬКО Caffeine (L1)
     * Использует @CaffeineOnly аннотацию
     */
    @CaffeineOnly(cacheName = "temp_data", ttl = 60)
    public String getTempData(String key) {
        return cacheRoutingService.getWithCaffeineOnly("temp_data", key, String.class, () -> {
            logger.info("Loading temp data from source: {}", key);
            return loadTempDataFromSource(key);
        });
    }

    /**
     * Сохранить временные данные - ТОЛЬКО Caffeine (L1)
     */
    @CaffeineOnly(cacheName = "temp_data", ttl = 60)
    public void setTempData(String key, String data) {
        cacheRoutingService.putWithCaffeineOnly("temp_data", key, data);
    }

    /**
     * Конфигурационные данные - ТОЛЬКО Caffeine (L1)
     * Редко изменяются, нужны быстро
     */
    @CaffeineOnly(cacheName = "config_data", ttl = 1800)
    public String getConfigData(String configKey) {
        return cacheRoutingService.getWithCaffeineOnly("config_data", configKey, String.class, () -> {
            logger.info("Loading config data from source: {}", configKey);
            return loadConfigDataFromSource(configKey);
        });
    }

    /**
     * Статистика - ДВУХУРОВНЕВЫЙ кеш с разными TTL
     */
    @TwoLevelCache(cacheName = "statistics", l1Ttl = 60, l2Ttl = 1800)
    public String getStatistics(String statKey) {
        return cacheRoutingService.getWithTwoLevelCache("statistics", statKey, String.class, () -> {
            logger.info("Loading statistics from source: {}", statKey);
            return loadStatisticsFromSource(statKey);
        });
    }

    /**
     * Обработка ошибок AmlClient
     * Использует только Redis, так как ошибки должны быть доступны всем экземплярам
     */
    public void processAmlClientErrors() {
        logger.info("Processing AmlClient errors with Redis-only routing");
        
        long startTime = System.currentTimeMillis();
        List<ClientRiskDto> errors = getAmlClientErrors();
        long endTime = System.currentTimeMillis();
        
        logger.info("Error retrieval took {} ms, found {} errors", 
            endTime - startTime, errors.size());

        // Обработка ошибок
        errors.forEach(this::processError);
    }

    /**
     * Обработка конкретной ошибки
     */
    private void processError(ClientRiskDto error) {
        String errorMessage = error.getError();
        
        if (errorMessage.contains(NOT_SUBJECT)) {
            handleNotSubjectError(error);
        } else if (errorMessage.equals(ERROR_FATAL)) {
            handleFatalError(error);
        } else if (errorMessage.equals(ERROR_SYNCHRONIZE)) {
            handleSynchronizeError(error);
        } else if (errorMessage.equals(ERROR_NOT_RESPONSE)) {
            handleNoResponseError(error);
        }
    }

    /**
     * Обработка ошибки "Сущность 'Субъект' не найдена"
     */
    private void handleNotSubjectError(ClientRiskDto error) {
        logger.warn("Handling NOT_SUBJECT error for partyId: {}", error.getPartyId());
        
        if (error.getAttempt() == 0) {
            // Обновляем attempt и сохраняем в Redis
            error.setAttempt(1);
            setAmlClient(error.getPartyId(), error);
        }
    }

    /**
     * Обработка фатальной ошибки
     */
    private void handleFatalError(ClientRiskDto error) {
        logger.error("Handling FATAL error for partyId: {}", error.getPartyId());
        // Ваша логика обработки фатальных ошибок
    }

    /**
     * Обработка ошибки синхронизации
     */
    private void handleSynchronizeError(ClientRiskDto error) {
        logger.warn("Handling SYNCHRONIZE error for partyId: {}", error.getPartyId());
        // Ваша логика обработки ошибок синхронизации
    }

    /**
     * Обработка ошибки отсутствия ответа
     */
    private void handleNoResponseError(ClientRiskDto error) {
        logger.warn("Handling NO_RESPONSE error for partyId: {}", error.getPartyId());
        // Ваша логика обработки отсутствия ответа
    }

    /**
     * Очистка кешей по типам
     */
    public void clearCaches() {
        logger.info("Clearing caches by type");
        
        // Очистить только Caffeine кеши
        cacheRoutingService.clear(new CacheRoute.CacheType[]{CacheRoute.CacheType.L1});
        
        // Redis кеш не очищаем полностью для безопасности
        logger.info("Redis cache not cleared for safety");
    }

    // Заглушки для загрузки данных из источников
    private String loadUserDataFromSource(String userId) {
        // Имитация задержки
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "User data for " + userId;
    }

    private String loadTempDataFromSource(String key) {
        return "Temp data for " + key;
    }

    private String loadConfigDataFromSource(String configKey) {
        return "Config data for " + configKey;
    }

    private String loadStatisticsFromSource(String statKey) {
        return "Statistics for " + statKey;
    }
}