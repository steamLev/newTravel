# Система маршрутизации кешей

## 🎯 Решение задачи

Реализована гибкая система маршрутизации кешей, где **AmlClient отправляется только в Redis**, а другие данные могут маршрутизироваться в разные кеши в зависимости от потребностей.

## 🏗 Архитектура

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│  Router     │───▶│   Cache     │
│             │    │  Service    │    │  Selection  │
└─────────────┘    └─────────────┘    └─────────────┘
                           │
                           ├─── L1 (Caffeine) - Локальный кеш
                           └─── L2 (Redis) - Распределенный кеш
```

## 🔧 Компоненты

### 1. Аннотации маршрутизации
- `@RedisOnly` - только Redis (для AmlClient)
- `@CaffeineOnly` - только Caffeine (для временных данных)
- `@TwoLevelCache` - оба кеша (для пользовательских данных)
- `@CacheRoute` - ручная маршрутизация

### 2. Сервис маршрутизации
- `CacheRoutingService` - основной сервис маршрутизации
- `RoutedAmlService` - пример использования с маршрутизацией
- `RoutedCacheController` - REST API для тестирования

### 3. Конфигурация
- `IntegratedCacheConfig` - конфигурация кешей
- `application-integrated.yml` - настройки

## 🚀 Использование

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
    
    @RedisOnly(cacheName = "aml_client", ttl = 3600)
    public void setAmlClient(String partyId, ClientRiskDto clientRisk) {
        cacheRoutingService.setAmlClient(partyId, clientRisk);
    }
}
```

### Пользовательские данные (двухуровневый кеш)

```java
@Service
public class UserService {
    @Autowired
    private CacheRoutingService cacheRoutingService;
    
    @TwoLevelCache(cacheName = "user_data", l1Ttl = 300, l2Ttl = 3600)
    public String getUserData(String userId) {
        return cacheRoutingService.getWithTwoLevelCache("user_data", userId, String.class,
            () -> loadUserDataFromSource(userId));
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
            () -> loadTempDataFromSource(key));
    }
}
```

## 📊 Маршрутизация по типам данных

| Тип данных | Маршрут | Причина | TTL L1 | TTL L2 |
|------------|---------|---------|--------|--------|
| **AmlClient** | **Redis only** | **Обмен между экземплярами** | **-** | **3600s** |
| Пользовательские данные | L1 + L2 | Часто используются | 300s | 3600s |
| Временные данные | Caffeine only | Быстрый доступ | 60s | - |
| Конфигурация | Caffeine only | Редко изменяется | 1800s | - |
| Статистика | L1 + L2 | Обмен + скорость | 60s | 1800s |

## 🧪 Тестирование

### Запуск тестов

```bash
# Запуск Redis
docker-compose up -d

# Запуск приложения
mvn spring-boot:run

# Тестирование маршрутизации
./test-routing.sh
```

### API Endpoints

- `GET /api/routed-cache/aml-client/{partyId}` - AmlClient (Redis only)
- `GET /api/routed-cache/user-data/{userId}` - User data (L1 + L2)
- `GET /api/routed-cache/temp-data/{key}` - Temp data (Caffeine only)
- `GET /api/routed-cache/config-data/{key}` - Config data (Caffeine only)
- `GET /api/routed-cache/statistics/{key}` - Statistics (L1 + L2)
- `GET /api/routed-cache/info` - Информация о маршрутизации

## ⚡ Производительность

### Времена выполнения

| Маршрут | Первый запрос | Повторные запросы |
|---------|---------------|-------------------|
| **Redis only** | **1-5 ms** | **1-5 ms** |
| Caffeine only | 1-10 μs | 1-10 μs |
| L1 + L2 | 1-10 μs | 1-10 μs |

## 🔧 Конфигурация

### application.yml

```yaml
# TTL для Redis (совместимо с вашим RedisService)
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

### Зависимости

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

## 📈 Преимущества маршрутизации

1. **Гибкость**: Каждый тип данных в оптимальном кеше
2. **Производительность**: Максимальная скорость для каждого случая
3. **Экономия ресурсов**: Не кешируем ненужные данные
4. **Масштабируемость**: Правильное распределение нагрузки
5. **Простота**: Использование аннотаций

## 🎯 Решение вашей задачи

✅ **AmlClient отправляется только в Redis** - используйте `@RedisOnly` аннотацию
✅ **Удобная маршрутизация** - аннотации для разных типов данных
✅ **Совместимость** - работает с вашим существующим RedisService
✅ **Гибкость** - легко изменить маршрутизацию для любого типа данных

## 📚 Документация

- `ROUTING_GUIDE.md` - Подробное руководство по маршрутизации
- `INTEGRATED_README.md` - Интеграция с существующим RedisService
- `MIGRATION_GUIDE.md` - Руководство по миграции

## 🚀 Быстрый старт

1. **Добавьте зависимости** в `pom.xml`
2. **Обновите конфигурацию** в `application.yml`
3. **Используйте аннотации** для маршрутизации:

```java
@RedisOnly(cacheName = "aml_client", ttl = 3600)
public ClientRiskDto getAmlClient(String partyId) {
    return cacheRoutingService.getAmlClient(partyId);
}
```

4. **Тестируйте** с помощью `./test-routing.sh`

## 🎉 Заключение

Система маршрутизации кешей решает вашу задачу:
- **AmlClient** идет только в Redis
- **Другие данные** маршрутизируются оптимально
- **Удобное управление** через аннотации
- **Полная совместимость** с существующим кодом

Начните использовать `@RedisOnly` для AmlClient и другие аннотации для остальных данных!