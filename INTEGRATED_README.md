# Интегрированный двухуровневый кеш с существующим RedisService

## 🎯 Обзор

Это решение интегрирует ваш существующий `RedisService` с двухуровневым кешем:
- **L1 (Caffeine)**: Быстрый локальный кеш в памяти
- **L2 (RedisService)**: Ваш существующий Redis сервис

## 🏗 Архитектура

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│  L1 Cache   │───▶│  L2 Cache   │───▶│  Database   │
│             │    │ (Caffeine)  │    │(RedisService)│    │             │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

## 🔧 Компоненты

### 1. Адаптер для RedisService
- `RedisServiceAdapter.java` - адаптер для интеграции с вашим RedisService
- `IntegratedTwoLevelCacheService.java` - основной сервис двухуровневого кеша
- `ClientRiskCacheService.java` - сервис для работы с ClientRiskDto

### 2. Конфигурация
- `IntegratedCacheConfig.java` - конфигурация совместимости
- `application-integrated.yml` - настройки для интеграции

### 3. Примеры использования
- `IntegratedAmlService.java` - пример интеграции с вашим AmlService
- `ClientRiskController.java` - REST API для тестирования

## 🚀 Быстрый старт

### 1. Добавьте зависимости

```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

### 2. Обновите конфигурацию

```yaml
# application.yml
ttl:
  redis: 3600

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
@Autowired
private RedisService redisService;

public ClientRiskDto getClientRisk(String key) {
    return redisService.getValue(key);
}
```

#### Стало:
```java
@Autowired
private ClientRiskCacheService clientRiskCacheService;

public ClientRiskDto getClientRisk(String key) {
    return clientRiskCacheService.getClientRisk(key);
}
```

## 📊 API совместимость

| RedisService | ClientRiskCacheService | Описание |
|--------------|------------------------|----------|
| `getValue(key)` | `getClientRisk(key)` | Получить значение |
| `setValue(key, value)` | `setClientRisk(key, value)` | Сохранить значение |
| `deleteValue(key)` | `deleteClientRisk(key)` | Удалить значение |
| `getErrorValue()` | `getErrorValues()` | Получить ошибки |

## 🎯 Преимущества

### 1. Производительность
- **L1 кеш**: 1-10 микросекунд (в 100-1000 раз быстрее Redis)
- **L2 кеш**: 1-5 миллисекунд (ваш существующий Redis)
- **Database**: 100+ миллисекунд

### 2. Надежность
- Graceful degradation при недоступности Redis
- Автоматический fallback: L1 → L2 → Database
- Обратная совместимость с существующим API

### 3. Мониторинг
- Детальное логирование операций кеширования
- Статистика производительности
- Проверка здоровья кеша

## 🔍 Примеры использования

### Простое кеширование

```java
@Service
public class YourService {
    @Autowired
    private ClientRiskCacheService cacheService;
    
    public ClientRiskDto getClientRisk(String partyId) {
        return cacheService.getClientRisk(partyId, () -> {
            // Ваша логика загрузки из источника
            return loadFromDatabase(partyId);
        });
    }
}
```

### Обработка ошибок

```java
public void processErrors() {
    List<ClientRiskDto> errors = cacheService.getErrorValues();
    
    errors.forEach(error -> {
        if (error.getError().contains("Фатальная ошибка")) {
            handleFatalError(error);
        }
    });
}
```

### Мониторинг

```java
@GetMapping("/health/cache")
public ResponseEntity<String> checkCacheHealth() {
    if (cacheService.isRedisAvailable()) {
        return ResponseEntity.ok("Cache is healthy");
    } else {
        return ResponseEntity.status(503).body("Redis unavailable");
    }
}
```

## 🧪 Тестирование

### Запуск тестов

```bash
# Запуск Redis
docker-compose up -d

# Запуск приложения
mvn spring-boot:run --spring.profiles.active=integrated

# Тестирование
./test-integration.sh
```

### API Endpoints

- `GET /api/client-risk/{key}` - Получить ClientRisk
- `POST /api/client-risk?key={key}` - Сохранить ClientRisk
- `DELETE /api/client-risk/{key}` - Удалить ClientRisk
- `GET /api/client-risk/errors` - Получить ошибки
- `GET /api/client-risk/cache/stats` - Статистика кеша
- `GET /api/client-risk/cache/health` - Здоровье кеша

## 📈 Мониторинг производительности

### Логи

```yaml
logging:
  level:
    com.example.cache: DEBUG
    kg.ssm.service.redis: DEBUG
```

### Статистика

```bash
# Получить статистику
curl http://localhost:8080/api/client-risk/cache/stats

# Проверить здоровье
curl http://localhost:8080/api/client-risk/cache/health
```

## 🔧 Конфигурация

### Caffeine (L1 Cache)

```yaml
cache:
  caffeine:
    spec: maximumSize=2000,expireAfterWrite=10m,recordStats
```

### Redis (L2 Cache)

```yaml
ttl:
  redis: 7200 # 2 часа в секундах
```

## 🚨 Troubleshooting

### Redis недоступен
- Приложение продолжит работать с L1 кешем
- Проверьте: `curl http://localhost:8080/api/client-risk/cache/health`

### Высокое потребление памяти
- Уменьшите `maximumSize` в Caffeine
- Настройте `expireAfterWrite`

### Медленные запросы
- Проверьте логи на наличие "Cache MISS"
- Убедитесь что Redis доступен

## 📚 Документация

- `MIGRATION_GUIDE.md` - Подробное руководство по миграции
- `EXAMPLES.md` - Примеры использования
- `README.md` - Основная документация

## 🎉 Заключение

Интегрированное решение обеспечивает:
- ✅ Полную совместимость с существующим RedisService
- ✅ Значительное улучшение производительности
- ✅ Повышенную надежность
- ✅ Простую миграцию
- ✅ Детальный мониторинг

Начните с замены `RedisService` на `ClientRiskCacheService` в ваших сервисах!