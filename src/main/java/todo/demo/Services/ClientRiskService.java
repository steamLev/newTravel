package todo.demo.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import todo.demo.Models.ClientRiskDto;
import todo.demo.Models.TempAmlEdit;
import todo.demo.Repositories.TempAmlEditRepository;

import java.util.List;

/**
 * Сервис для работы с клиентскими рисками с использованием системы маршрутизации кэша
 */
@Slf4j
@Service
public class ClientRiskService {
    
    private final CacheRoutingService cacheRoutingService;
    private final TempAmlEditRepository tempAmlEditRepository;
    
    @Autowired
    public ClientRiskService(CacheRoutingService cacheRoutingService, 
                           TempAmlEditRepository tempAmlEditRepository) {
        this.cacheRoutingService = cacheRoutingService;
        this.tempAmlEditRepository = tempAmlEditRepository;
    }
    
    /**
     * Устанавливает значение в кэш с автоматической маршрутизацией
     * @param key ключ
     * @param value значение
     */
    public void setValue(String key, Object value) {
        cacheRoutingService.setValue(key, value);
    }
    
    /**
     * Получает значение из кэша с автоматической маршрутизацией
     * @param key ключ
     * @return ClientRiskDto или null если не найдено
     */
    public ClientRiskDto getValue(String key) {
        return cacheRoutingService.getValue(key);
    }
    
    /**
     * Получает все записи с ошибками из кэша
     * @return список записей с ошибками
     */
    public List<ClientRiskDto> getErrorValue() {
        return cacheRoutingService.getErrorValue();
    }
    
    /**
     * Обрабатывает ошибки NOT_SUBJECT и сохраняет в базу данных
     * @param clientRiskDto запись с ошибкой
     */
    public void processNotSubjectError(ClientRiskDto clientRiskDto) {
        if (clientRiskDto.getAttempt() == 0) {
            try {
                // Сохраняем в базу данных
                TempAmlEdit tempAmlEdit = TempAmlEdit.buildTempAmlEdit(clientRiskDto);
                tempAmlEditRepository.save(tempAmlEdit);
                
                // Обновляем попытку и сохраняем в кэш
                clientRiskDto.setAttempt(1);
                cacheRoutingService.setValue("ERROR_CHECK_" + clientRiskDto.getPartyId(), clientRiskDto);
                
                log.info("Processed NOT_SUBJECT error for partyId: {}", clientRiskDto.getPartyId());
            } catch (Exception e) {
                log.error("Error processing NOT_SUBJECT error for partyId: {}", 
                         clientRiskDto.getPartyId(), e);
            }
        }
    }
    
    /**
     * Получает статистику кэша
     * @return статистика в виде строки
     */
    public String getCacheStats() {
        return cacheRoutingService.getCacheStats();
    }
    
    /**
     * Очищает кэш
     */
    public void clearCache() {
        cacheRoutingService.clearCache();
    }
    
    /**
     * Проверяет существование ключа в кэше
     * @param key ключ
     * @return true если ключ существует
     */
    public boolean hasKey(String key) {
        return cacheRoutingService.hasKey(key);
    }
    
    /**
     * Удаляет значение из кэша
     * @param key ключ
     */
    public void deleteValue(String key) {
        cacheRoutingService.deleteValue(key);
    }
}