package todo.demo.Services;

import todo.demo.Models.ClientRiskDto;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для системы маршрутизации кэша Redis-Caffeine
 */
public interface CacheRoutingService {
    
    /**
     * Устанавливает значение в кэш с автоматической маршрутизацией
     * @param key ключ
     * @param value значение
     */
    void setValue(String key, Object value);
    
    /**
     * Получает значение из кэша с автоматической маршрутизацией
     * @param key ключ
     * @return значение или null если не найдено
     */
    ClientRiskDto getValue(String key);
    
    /**
     * Получает значение из кэша с указанием типа
     * @param key ключ
     * @param clazz класс для десериализации
     * @param <T> тип возвращаемого значения
     * @return значение или null если не найдено
     */
    <T> T getValue(String key, Class<T> clazz);
    
    /**
     * Получает все записи с ошибками из кэша
     * @return список записей с ошибками
     */
    List<ClientRiskDto> getErrorValue();
    
    /**
     * Удаляет значение из кэша
     * @param key ключ
     */
    void deleteValue(String key);
    
    /**
     * Проверяет существование ключа в кэше
     * @param key ключ
     * @return true если ключ существует
     */
    boolean hasKey(String key);
    
    /**
     * Получает все ключи по паттерну
     * @param pattern паттерн поиска
     * @return множество ключей
     */
    java.util.Set<String> getKeysByPattern(String pattern);
    
    /**
     * Очищает весь кэш
     */
    void clearCache();
    
    /**
     * Получает статистику кэша
     * @return статистика в виде строки
     */
    String getCacheStats();
}