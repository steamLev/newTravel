# Руководство по миграции на интегрированный двухуровневый кеш

## Обзор изменений

Ваш существующий `RedisService` интегрирован с двухуровневым кешем:
- **L1 (Caffeine)**: Быстрый локальный кеш
- **L2 (RedisService)**: Ваш существующий Redis сервис

## Шаги миграции

### 1. Добавьте зависимости

В ваш `pom.xml` добавьте:

```xml
<!-- Caffeine Cache -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>

<!-- Spring Boot Cache -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

### 2. Обновите конфигурацию

В `application.yml` добавьте:

```yaml
# TTL для Redis (уже есть в вашем проекте)
ttl:
  redis: 3600

# Cache Configuration
cache:
  caffeine:
    spec: maximumSize=1000,expireAfterWrite=5m,recordStats
  redis:
    default-ttl: 3600000
    key-prefix: "ClientAml "
```

### 3. Замените использование RedisService

#### Было:
```java
@Service
public class YourService {
    @Autowired
    private RedisService redisService;
    
    public ClientRiskDto getClientRisk(String key) {
        return redisService.getValue(key);
    }
    
    public void setClientRisk(String key, ClientRiskDto value) {
        redisService.setValue(key, value);
    }
}
```

#### Стало:
```java
@Service
public class YourService {
    @Autowired
    private ClientRiskCacheService clientRiskCacheService;
    
    public ClientRiskDto getClientRisk(String key) {
        return clientRiskCacheService.getClientRisk(key);
    }
    
    public void setClientRisk(String key, ClientRiskDto value) {
        clientRiskCacheService.setClientRisk(key, value);
    }
}
```

### 4. Используйте новые возможности

#### Кеширование с fallback:
```java
// Автоматический fallback: L1 -> L2 -> Database
ClientRiskDto clientRisk = clientRiskCacheService.getClientRisk(key, () -> {
    // Ваша логика загрузки из базы данных
    return loadFromDatabase(key);
});
```

#### Проверка доступности Redis:
```java
if (clientRiskCacheService.isRedisAvailable()) {
    // Redis доступен
} else {
    // Работаем только с L1 кешем
}
```

## API совместимость

### Существующие методы RedisService

| RedisService | ClientRiskCacheService | Описание |
|--------------|------------------------|----------|
| `getValue(key)` | `getClientRisk(key)` | Получить значение |
| `setValue(key, value)` | `setClientRisk(key, value)` | Сохранить значение |
| `deleteValue(key)` | `deleteClientRisk(key)` | Удалить значение |
| `getErrorValue()` | `getErrorValues()` | Получить ошибки |

### Новые возможности

| Метод | Описание |
|-------|----------|
| `getClientRisk(key, valueLoader)` | Кеширование с кастомной загрузкой |
| `clearClientRiskCache()` | Очистка кеша |
| `isRedisAvailable()` | Проверка доступности Redis |
| `getCacheStats()` | Статистика кеша |

## Примеры использования

### 1. Простое кеширование

```java
@Service
public class AmlService {
    @Autowired
    private ClientRiskCacheService cacheService;
    
    public ClientRiskDto processClientRisk(String partyId) {
        // Получаем из кеша или загружаем
        return cacheService.getClientRisk(partyId, () -> {
            // Логика загрузки из источника
            return loadClientRiskFromSource(partyId);
        });
    }
}
```

### 2. Обработка ошибок

```java
@Service
public class ErrorProcessingService {
    @Autowired
    private ClientRiskCacheService cacheService;
    
    public void processErrors() {
        List<ClientRiskDto> errors = cacheService.getErrorValues();
        
        errors.forEach(error -> {
            if (error.getError().contains("Фатальная ошибка")) {
                // Обработка фатальных ошибок
                handleFatalError(error);
            }
        });
    }
}
```

### 3. Мониторинг

```java
@RestController
public class HealthController {
    @Autowired
    private ClientRiskCacheService cacheService;
    
    @GetMapping("/health/cache")
    public ResponseEntity<String> checkCacheHealth() {
        if (cacheService.isRedisAvailable()) {
            return ResponseEntity.ok("Cache is healthy");
        } else {
            return ResponseEntity.status(503).body("Redis unavailable");
        }
    }
}
```

## Конфигурация производительности

### Caffeine (L1 Cache)

```yaml
cache:
  caffeine:
    spec: maximumSize=2000,expireAfterWrite=10m,recordStats
```

Параметры:
- `maximumSize=2000` - увеличить для больших объемов данных
- `expireAfterWrite=10m` - TTL после записи
- `expireAfterAccess=5m` - TTL после последнего доступа

### Redis (L2 Cache)

```yaml
ttl:
  redis: 7200 # 2 часа в секундах
```

## Мониторинг и отладка

### Логирование

```yaml
logging:
  level:
    com.example.cache: DEBUG
    kg.ssm.service.redis: DEBUG
```

### Статистика

```java
@GetMapping("/cache/stats")
public String getStats() {
    return clientRiskCacheService.getCacheStats();
}
```

## Преимущества миграции

1. **Производительность**: L1 кеш в 100-1000 раз быстрее Redis
2. **Надежность**: Graceful degradation при недоступности Redis
3. **Совместимость**: Полная совместимость с существующим API
4. **Мониторинг**: Детальная статистика и логирование
5. **Гибкость**: Легко настраиваемые параметры

## Обратная совместимость

- Все существующие методы RedisService работают как раньше
- Конфигурация TTL остается прежней
- Префиксы ключей не изменились
- Логика обработки ошибок сохранена

## Тестирование

```bash
# Тест производительности
curl http://localhost:8080/api/client-risk/test-key

# Тест ошибок
curl http://localhost:8080/api/client-risk/errors

# Тест статистики
curl http://localhost:8080/api/client-risk/cache/stats
```