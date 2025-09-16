package todo.demo.Services.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Service;
import todo.demo.Models.ClientRiskDto;
import todo.demo.Services.CacheRoutingService;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CacheRoutingServiceImpl implements CacheRoutingService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final Cache<String, Object> localCache;
    
    // Константы для типов ошибок
    private static final String ERROR = "ERROR";
    private static final String NOT_SUBJECT = "NOT_SUBJECT";
    private static final String ERROR_FATAL = "ERROR_FATAL";
    private static final String ERROR_SYNCHRONIZE = "ERROR_SYNCHRONIZE";
    private static final String ERROR_NOT_RESPONSE = "ERROR_NOT_RESPONSE";
    private static final String ERROR_CHECK = "ERROR_CHECK_";
    private static final String KEY = "KEY_";
    
    // TTL для Redis (в секундах)
    private static final long TTL = 3600; // 1 час
    
    @Autowired
    public CacheRoutingServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        // Настройка локального кэша Caffeine
        this.localCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
    
    @Override
    public void setValue(String key, Object value) {
        try {
            // Сначала сохраняем в Redis
            redisTemplate.opsForValue().set(key, value, TTL, TimeUnit.SECONDS);
            
            // Затем сохраняем в локальный кэш для быстрого доступа
            localCache.put(key, value);
            
            log.debug("Value set for key: {} in both Redis and local cache", key);
        } catch (Exception e) {
            log.error("Error setting value for key: {}", key, e);
            // Fallback: сохраняем только в локальный кэш
            localCache.put(key, value);
        }
    }
    
    @Override
    public ClientRiskDto getValue(String key) {
        return getValue(key, ClientRiskDto.class);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key, Class<T> clazz) {
        try {
            // Сначала проверяем локальный кэш
            Object value = localCache.getIfPresent(key);
            if (value != null) {
                log.debug("Value found in local cache for key: {}", key);
                return (T) value;
            }
            
            // Если нет в локальном кэше, проверяем Redis
            value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                // Сохраняем в локальный кэш для будущих запросов
                localCache.put(key, value);
                log.debug("Value found in Redis and cached locally for key: {}", key);
                return (T) value;
            }
            
            log.debug("Value not found for key: {}", key);
            return null;
        } catch (Exception e) {
            log.error("Error getting value for key: {}", key, e);
            return null;
        }
    }
    
    @Override
    public List<ClientRiskDto> getErrorValue() {
        try {
            // Получаем все ключи ошибок из Redis
            Set<byte[]> keysRaw = redisTemplate.execute((RedisCallback<Set<byte[]>>) connection -> 
                connection.keys((ERROR + "*").getBytes()));
            
            if (keysRaw == null || keysRaw.isEmpty()) {
                return Collections.emptyList();
            }
            
            Set<String> keys = keysRaw.stream()
                    .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                    .collect(Collectors.toSet());
            
            // Обрабатываем записи с ошибкой NOT_SUBJECT
            keys.stream()
                    .map(this::getValue)
                    .filter(Objects::nonNull)
                    .filter(clientRiskDto -> clientRiskDto.getError() != null && 
                            clientRiskDto.getError().contains(NOT_SUBJECT))
                    .forEach(this::processNotSubjectError);
            
            // Фильтруем записи с критическими ошибками
            List<ClientRiskDto> criticalErrors = keys.stream()
                    .map(this::getValue)
                    .filter(Objects::nonNull)
                    .filter(clientRiskDto -> isCriticalError(clientRiskDto.getError()))
                    .collect(Collectors.toList());
            
            // Получаем записи, которые не отправились
            return criticalErrors.stream()
                    .map(clientRiskDto -> Optional.ofNullable(getValue(KEY + clientRiskDto.getPartyId())))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting error values", e);
            return Collections.emptyList();
        }
    }
    
    private void processNotSubjectError(ClientRiskDto clientRiskDto) {
        if (clientRiskDto.getAttempt() == 0) {
            // Здесь должна быть логика сохранения в TempAmlEditRepository
            // tempAmlEditRepository.save(TempAmlEdit.buildTempAmlEdit(clientRiskDto));
            
            clientRiskDto.setAttempt(1);
            setValue(ERROR_CHECK + clientRiskDto.getPartyId(), clientRiskDto);
            
            log.info("Processed NOT_SUBJECT error for partyId: {}", clientRiskDto.getPartyId());
        }
    }
    
    private boolean isCriticalError(String error) {
        return ERROR_FATAL.equals(error) || 
               ERROR_SYNCHRONIZE.equals(error) || 
               ERROR_NOT_RESPONSE.equals(error);
    }
    
    @Override
    public void deleteValue(String key) {
        try {
            // Удаляем из Redis
            redisTemplate.delete(key);
            
            // Удаляем из локального кэша
            localCache.invalidate(key);
            
            log.debug("Value deleted for key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting value for key: {}", key, e);
        }
    }
    
    @Override
    public boolean hasKey(String key) {
        try {
            // Проверяем сначала локальный кэш
            if (localCache.getIfPresent(key) != null) {
                return true;
            }
            
            // Проверяем Redis
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking key existence: {}", key, e);
            return false;
        }
    }
    
    @Override
    public Set<String> getKeysByPattern(String pattern) {
        try {
            Set<byte[]> keysRaw = redisTemplate.execute((RedisCallback<Set<byte[]>>) connection -> 
                connection.keys(pattern.getBytes()));
            
            if (keysRaw == null || keysRaw.isEmpty()) {
                return Collections.emptySet();
            }
            
            return keysRaw.stream()
                    .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error getting keys by pattern: {}", pattern, e);
            return Collections.emptySet();
        }
    }
    
    @Override
    public void clearCache() {
        try {
            // Очищаем локальный кэш
            localCache.invalidateAll();
            
            // Очищаем Redis (осторожно!)
            Set<String> keys = getKeysByPattern("*");
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            log.info("Cache cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing cache", e);
        }
    }
    
    @Override
    public String getCacheStats() {
        try {
            com.github.benmanes.caffeine.cache.stats.CacheStats stats = localCache.stats();
            
            return String.format(
                "Local Cache Stats:\n" +
                "Hit Rate: %.2f%%\n" +
                "Miss Rate: %.2f%%\n" +
                "Request Count: %d\n" +
                "Hit Count: %d\n" +
                "Miss Count: %d\n" +
                "Eviction Count: %d\n" +
                "Average Load Time: %.2f ms",
                stats.hitRate() * 100,
                stats.missRate() * 100,
                stats.requestCount(),
                stats.hitCount(),
                stats.missCount(),
                stats.evictionCount(),
                stats.averageLoadPenalty() / 1_000_000.0
            );
        } catch (Exception e) {
            log.error("Error getting cache stats", e);
            return "Error retrieving cache stats";
        }
    }
}