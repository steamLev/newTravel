package com.example.cache.service;

import kg.ssm.domain.redis.ClientRiskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Пример интеграции с существующим AmlService
 * Показывает как использовать интегрированный кеш в реальном сервисе
 */
@Service
public class IntegratedAmlService {

    private static final Logger logger = LoggerFactory.getLogger(IntegratedAmlService.class);

    @Autowired
    private ClientRiskCacheService clientRiskCacheService;

    // Константы из вашего RedisService
    private static final String ERROR_FATAL = "Фатальная ошибка при сохранении в АБС:";
    private static final String ERROR_SYNCHRONIZE = "Ошибка при синхронизации клиента subjectId:";
    private static final String ERROR_NOT_RESPONSE = "Нет ответа от ABS";
    private static final String NOT_SUBJECT = "Сущность 'Субъект' по указанному PartyId";
    private static final String KEY_PREFIX = "ClientAml ";
    private static final String ERROR_PREFIX = "Error";

    /**
     * Получить ClientRiskDto с кешированием
     * Заменяет прямой вызов redisService.getValue()
     */
    public ClientRiskDto getClientRisk(String partyId) {
        String key = KEY_PREFIX + partyId;
        
        return clientRiskCacheService.getClientRisk(key, () -> {
            logger.info("Loading ClientRisk from source for partyId: {}", partyId);
            // Здесь ваша логика загрузки из источника данных
            return loadClientRiskFromSource(partyId);
        });
    }

    /**
     * Сохранить ClientRiskDto с кешированием
     * Заменяет прямой вызов redisService.setValue()
     */
    public void setClientRisk(String partyId, ClientRiskDto clientRisk) {
        String key = KEY_PREFIX + partyId;
        clientRiskCacheService.setClientRisk(key, clientRisk);
    }

    /**
     * Удалить ClientRiskDto из кеша
     * Заменяет прямой вызов redisService.deleteValue()
     */
    public void deleteClientRisk(String partyId) {
        String key = KEY_PREFIX + partyId;
        clientRiskCacheService.deleteClientRisk(key);
    }

    /**
     * Получить список ошибок с кешированием
     * Заменяет прямой вызов redisService.getErrorValue()
     */
    public List<ClientRiskDto> getErrorValues() {
        return clientRiskCacheService.getErrorValues();
    }

    /**
     * Обработка ошибок с улучшенной производительностью
     * Использует кеширование для быстрого доступа к данным
     */
    public void processErrors() {
        logger.info("Processing errors with integrated cache");
        
        long startTime = System.currentTimeMillis();
        List<ClientRiskDto> errors = getErrorValues();
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
        
        // Ваша логика обработки
        if (error.getAttempt() == 0) {
            // Сохраняем в TempAmlEdit
            // tempAmlEditRepository.save(TempAmlEdit.buildTempAmlEdit(error));
            
            // Обновляем attempt и сохраняем в кеш
            error.setAttempt(1);
            setClientRisk(error.getPartyId(), error);
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
     * Проверить здоровье кеша
     */
    public boolean isCacheHealthy() {
        return clientRiskCacheService.isRedisAvailable();
    }

    /**
     * Получить статистику кеша
     */
    public String getCacheStatistics() {
        return clientRiskCacheService.getCacheStats();
    }

    /**
     * Очистить кеш (для административных целей)
     */
    public void clearCache() {
        logger.info("Clearing integrated cache");
        clientRiskCacheService.clearClientRiskCache();
    }

    /**
     * Загрузка ClientRiskDto из источника данных
     * Заглушка - замените на вашу реальную логику
     */
    private ClientRiskDto loadClientRiskFromSource(String partyId) {
        // Имитация задержки загрузки из источника
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Создаем заглушку
        ClientRiskDto clientRisk = new ClientRiskDto();
        // Установите необходимые поля
        // clientRisk.setPartyId(partyId);
        // clientRisk.setError("Loaded from source");
        
        logger.debug("Loaded ClientRisk from source for partyId: {}", partyId);
        return clientRisk;
    }
}