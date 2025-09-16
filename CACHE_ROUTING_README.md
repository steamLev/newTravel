# Система маршрутизации кэша Redis-Caffeine

## Описание

Удобная и простая система для маршрутизации кэша, которая автоматически выбирает между Redis (распределенный кэш) и Caffeine (локальный кэш) для оптимальной производительности.

## Особенности

- **Двухуровневое кэширование**: Локальный кэш (Caffeine) + распределенный кэш (Redis)
- **Автоматическая маршрутизация**: Система сама выбирает оптимальный источник данных
- **Fallback механизм**: При недоступности Redis работает только с локальным кэшем
- **Статистика производительности**: Детальная информация о работе кэша
- **Простой API**: Легко интегрируется с существующим кодом

## Архитектура

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Application   │───▶│  CacheRouting   │───▶│   Local Cache   │
│                 │    │    Service      │    │   (Caffeine)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │  Redis Cache    │
                       │ (Distributed)   │
                       └─────────────────┘
```

## Конфигурация

### 1. Зависимости (уже добавлены в pom.xml)

```xml
<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Caffeine Cache -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>

<!-- Spring Cache -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

### 2. Настройки в application.properties

```properties
# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
spring.data.redis.timeout=2000ms

# Cache Configuration
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=30m,expireAfterAccess=10m
```

## Использование

### 1. Базовое использование

```java
@Autowired
private ClientRiskService clientRiskService;

// Установка значения
ClientRiskDto dto = new ClientRiskDto("123", "ERROR_FATAL", 0);
clientRiskService.setValue("key123", dto);

// Получение значения
ClientRiskDto result = clientRiskService.getValue("key123");

// Проверка существования ключа
boolean exists = clientRiskService.hasKey("key123");

// Удаление значения
clientRiskService.deleteValue("key123");
```

### 2. Работа с ошибками

```java
// Получение всех записей с ошибками
List<ClientRiskDto> errors = clientRiskService.getErrorValue();

// Обработка ошибок NOT_SUBJECT
for (ClientRiskDto error : errors) {
    if (error.getError().contains("NOT_SUBJECT")) {
        clientRiskService.processNotSubjectError(error);
    }
}
```

### 3. Мониторинг и статистика

```java
// Получение статистики кэша
String stats = clientRiskService.getCacheStats();
System.out.println(stats);

// Очистка кэша
clientRiskService.clearCache();
```

## API Endpoints

### Установка значения
```
POST /api/cache/set?key=test123
Content-Type: application/json

{
    "partyId": "123",
    "error": "ERROR_FATAL",
    "attempt": 0
}
```

### Получение значения
```
GET /api/cache/get?key=test123
```

### Получение всех ошибок
```
GET /api/cache/errors
```

### Статистика кэша
```
GET /api/cache/stats
```

### Проверка существования ключа
```
GET /api/cache/has-key?key=test123
```

### Удаление значения
```
DELETE /api/cache/delete?key=test123
```

### Очистка кэша
```
POST /api/cache/clear
```

## Интеграция с существующим кодом

Замените ваш существующий код:

```java
// Старый код
public void setValue(String key, Object value) {
    redisTemplate.opsForValue().set(key, value, TTL, TimeUnit.SECONDS);
}

public ClientRiskDto getValue(String key) {
    return (ClientRiskDto) redisTemplate.opsForValue().get(key);
}
```

На новый:

```java
// Новый код с маршрутизацией
@Autowired
private ClientRiskService clientRiskService;

public void setValue(String key, Object value) {
    clientRiskService.setValue(key, value);
}

public ClientRiskDto getValue(String key) {
    return clientRiskService.getValue(key);
}
```

## Преимущества

1. **Производительность**: Локальный кэш обеспечивает быстрый доступ к часто используемым данным
2. **Масштабируемость**: Redis обеспечивает распределенное кэширование
3. **Надежность**: Fallback на локальный кэш при недоступности Redis
4. **Мониторинг**: Детальная статистика работы кэша
5. **Простота**: Минимальные изменения в существующем коде

## Мониторинг

Система предоставляет детальную статистику:

- Hit Rate (процент попаданий)
- Miss Rate (процент промахов)
- Количество запросов
- Количество попаданий и промахов
- Количество вытеснений
- Среднее время загрузки

## Требования

- Java 21+
- Spring Boot 3.2.6+
- Redis (опционально, система работает и без него)
- PostgreSQL (для TempAmlEdit)

## Запуск

1. Убедитесь, что Redis запущен (опционально)
2. Запустите приложение
3. Используйте API endpoints для тестирования

## Примеры использования

Смотрите `CacheController` для примеров REST API и `ClientRiskService` для примеров программного использования.