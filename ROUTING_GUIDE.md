# Руководство по маршрутизации кешей

## 🎯 Обзор

Система маршрутизации кешей позволяет гибко управлять тем, какие данные в какие кеши отправляются. Это обеспечивает оптимальную производительность и использование ресурсов.

## 🏗 Архитектура маршрутизации

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│  Router     │───▶│   Cache     │
│             │    │  Service    │    │  Selection  │
└─────────────┘    └─────────────┘    └─────────────┘
                           │
                           ├─── L1 (Caffeine) - Локальный кеш
                           └─── L2 (Redis) - Распределенный кеш
```

## 🔧 Типы маршрутизации

### 1. Только Redis (L2) - `@RedisOnly`
**Использование**: AmlClient, данные для обмена между экземплярами
**Преимущества**: Доступность для всех экземпляров, персистентность
**Недостатки**: Медленнее локального кеша

```java
@RedisOnly(cacheName = "aml_client", ttl = 3600)
public ClientRiskDto getAmlClient(String partyId) {
    return cacheRoutingService.getAmlClient(partyId);
}
```

### 2. Только Caffeine (L1) - `@CaffeineOnly`
**Использование**: Временные данные, конфигурация, кеш-специфичные данные
**Преимущества**: Максимальная скорость, низкое потребление ресурсов
**Недостатки**: Недоступно другим экземплярам

```java
@CaffeineOnly(cacheName = "temp_data", ttl = 60)
public String getTempData(String key) {
    return cacheRoutingService.getWithCaffeineOnly("temp_data", key, String.class, 
        () -> loadTempDataFromSource(key));
}
```

### 3. Двухуровневый кеш (L1 + L2) - `@TwoLevelCache`
**Использование**: Пользовательские данные, часто используемые данные
**Преимущества**: Максимальная скорость + доступность
**Недостатки**: Больше потребления ресурсов

```java
@TwoLevelCache(cacheName = "user_data", l1Ttl = 300, l2Ttl = 3600)
public String getUserData(String userId) {
    return cacheRoutingService.getWithTwoLevelCache("user_data", userId, String.class,
        () -> loadUserDataFromSource(userId));
}
```

## 📊 Рекомендации по маршрутизации

| Тип данных | Маршрут | Причина | TTL L1 | TTL L2 |
|------------|---------|---------|--------|--------|
| AmlClient | Redis only | Обмен между экземплярами | - | 3600s |
| Пользовательские данные | L1 + L2 | Часто используются | 300s | 3600s |
| Временные данные | Caffeine only | Быстрый доступ | 60s | - |
| Конфигурация | Caffeine only | Редко изменяется | 1800s | - |
| Статистика | L1 + L2 | Обмен + скорость | 60s | 1800s |
| Сессии | Caffeine only | Локальные данные | 1800s | - |
| Кеш ошибок | Redis only | Обмен между экземплярами | - | 7200s |

## 🚀 Использование аннотаций

### @RedisOnly
```java
@RedisOnly(cacheName = "aml_client", ttl = 3600)
public ClientRiskDto getAmlClient(String partyId) {
    // Автоматически маршрутизируется только в Redis
}
```

### @CaffeineOnly
```java
@CaffeineOnly(cacheName = "config", ttl = 1800)
public String getConfig(String key) {
    // Автоматически маршрутизируется только в Caffeine
}
```

### @TwoLevelCache
```java
@TwoLevelCache(cacheName = "user_data", l1Ttl = 300, l2Ttl = 3600)
public String getUserData(String userId) {
    // Автоматически маршрутизируется в оба кеша
}
```

### @CacheRoute (ручная маршрутизация)
```java
@CacheRoute({CacheRoute.CacheType.L1, CacheRoute.CacheType.L2})
public String getCustomData(String key) {
    // Ручная маршрутизация
}
```

## 🔧 Программная маршрутизация

### Прямое использование CacheRoutingService

```java
@Service
public class MyService {
    @Autowired
    private CacheRoutingService cacheRoutingService;
    
    // Только Redis
    public ClientRiskDto getAmlClient(String key) {
        return cacheRoutingService.getAmlClient(key);
    }
    
    // Двухуровневый кеш
    public String getUserData(String key) {
        return cacheRoutingService.getWithTwoLevelCache("user_data", key, String.class,
            () -> loadFromSource(key));
    }
    
    // Только Caffeine
    public String getTempData(String key) {
        return cacheRoutingService.getWithCaffeineOnly("temp_data", key, String.class,
            () -> loadFromSource(key));
    }
    
    // Кастомная маршрутизация
    public String getCustomData(String key) {
        return cacheRoutingService.get("custom", key, String.class,
            new CacheRoute.CacheType[]{CacheRoute.CacheType.L1}, 
            () -> loadFromSource(key));
    }
}
```

## 📈 Мониторинг маршрутизации

### Логирование
```yaml
logging:
  level:
    com.example.cache.service.CacheRoutingService: DEBUG
```

### Статистика
```java
@GetMapping("/cache/stats")
public String getCacheStats() {
    return "L1 Cache: " + l1CacheManager.getCacheNames() + 
           ", Redis: " + (redisServiceAdapter.isAvailable() ? "Available" : "Unavailable");
}
```

## 🧪 Тестирование маршрутизации

### Запуск тестов
```bash
# Запуск приложения
mvn spring-boot:run

# Тестирование маршрутизации
./test-routing.sh
```

### API Endpoints для тестирования

- `GET /api/routed-cache/aml-client/{partyId}` - AmlClient (Redis only)
- `GET /api/routed-cache/user-data/{userId}` - User data (L1 + L2)
- `GET /api/routed-cache/temp-data/{key}` - Temp data (Caffeine only)
- `GET /api/routed-cache/config-data/{key}` - Config data (Caffeine only)
- `GET /api/routed-cache/statistics/{key}` - Statistics (L1 + L2)
- `GET /api/routed-cache/info` - Информация о маршрутизации

## ⚡ Производительность

### Времена выполнения (примерные)

| Маршрут | Первый запрос | Повторные запросы |
|---------|---------------|-------------------|
| Redis only | 1-5 ms | 1-5 ms |
| Caffeine only | 1-10 μs | 1-10 μs |
| L1 + L2 | 1-10 μs | 1-10 μs |

### Оптимизация

1. **Выбор правильного маршрута**:
   - Часто используемые данные → L1 + L2
   - Редко используемые данные → L2 only
   - Временные данные → L1 only

2. **Настройка TTL**:
   - Короткий TTL для часто изменяющихся данных
   - Длинный TTL для стабильных данных

3. **Размер кешей**:
   - L1: 1000-5000 записей
   - L2: Ограничен только памятью Redis

## 🚨 Troubleshooting

### Проблема: Данные не кешируются
**Решение**: Проверьте аннотации и конфигурацию кешей

### Проблема: Медленные запросы
**Решение**: Убедитесь что используется правильный маршрут для типа данных

### Проблема: Данные не синхронизируются между экземплярами
**Решение**: Используйте Redis only для данных, которые должны быть общими

## 📚 Примеры использования

### AmlClient (только Redis)
```java
@Service
public class AmlService {
    @Autowired
    private CacheRoutingService cacheRoutingService;
    
    @RedisOnly(cacheName = "aml_client", ttl = 3600)
    public ClientRiskDto getAmlClient(String partyId) {
        return cacheRoutingService.getAmlClient(partyId);
    }
    
    public void processAmlClientErrors() {
        List<ClientRiskDto> errors = cacheRoutingService.getAmlClientErrors();
        errors.forEach(this::processError);
    }
}
```

### Пользовательские данные (двухуровневый)
```java
@Service
public class UserService {
    @Autowired
    private CacheRoutingService cacheRoutingService;
    
    @TwoLevelCache(cacheName = "user_data", l1Ttl = 300, l2Ttl = 3600)
    public User getUser(String userId) {
        return cacheRoutingService.getWithTwoLevelCache("user_data", userId, User.class,
            () -> userRepository.findById(userId));
    }
}
```

### Временные данные (только Caffeine)
```java
@Service
public class TempService {
    @Autowired
    private CacheRoutingService cacheRoutingService;
    
    @CaffeineOnly(cacheName = "temp_data", ttl = 60)
    public String getTempData(String key) {
        return cacheRoutingService.getWithCaffeineOnly("temp_data", key, String.class,
            () -> generateTempData(key));
    }
}
```

## 🎉 Заключение

Система маршрутизации кешей обеспечивает:
- ✅ Гибкое управление кешированием
- ✅ Оптимальную производительность
- ✅ Эффективное использование ресурсов
- ✅ Простоту использования через аннотации
- ✅ Детальный мониторинг и отладку

Используйте правильный маршрут для каждого типа данных!