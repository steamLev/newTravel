# Примеры использования двухуровневого кеша

## Быстрый старт

1. **Запуск Redis**:
   ```bash
   docker-compose up -d
   ```

2. **Запуск приложения**:
   ```bash
   mvn spring-boot:run
   ```

3. **Тестирование**:
   ```bash
   ./test-api.sh
   ```

## Примеры запросов

### 1. Тестирование кеширования пользователей

```bash
# Первый запрос - загрузка из БД (медленно)
curl http://localhost:8080/api/users/1

# Второй запрос - загрузка из L1 кеша (быстро)
curl http://localhost:8080/api/users/1

# Третий запрос - загрузка из L1 кеша (быстро)
curl http://localhost:8080/api/users/1
```

### 2. Тестирование кеширования продуктов

```bash
# Поиск по категории
curl "http://localhost:8080/api/products/category/Электроника"

# Поиск по ценовому диапазону
curl "http://localhost:8080/api/products/price-range?min=1000&max=20000"

# Поиск по названию
curl "http://localhost:8080/api/products/search?q=ноутбук"
```

### 3. Создание и обновление данных

```bash
# Создание пользователя
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Новый",
    "lastName": "Пользователь",
    "email": "new@example.com"
  }'

# Создание продукта
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Новый продукт",
    "category": "Тест",
    "price": 1000,
    "stock": 5
  }'
```

### 4. Мониторинг кеша

```bash
# Общая статистика
curl http://localhost:8080/api/cache/stats

# Статистика Caffeine
curl http://localhost:8080/api/cache/stats/caffeine

# Информация о Redis
curl http://localhost:8080/api/cache/stats/redis
```

## Анализ производительности

### Времена выполнения (примерные)

| Операция | L1 Cache (Caffeine) | L2 Cache (Redis) | Database |
|----------|---------------------|------------------|----------|
| Чтение   | 1-10 μs            | 1-5 ms          | 100+ ms  |
| Запись   | 1-10 μs            | 1-5 ms          | 100+ ms  |

### Логи для анализа

```bash
# Включите DEBUG логирование для детального анализа
# В application.yml:
logging:
  level:
    com.example.cache: DEBUG
    org.springframework.cache: DEBUG
```

Примеры логов:
```
DEBUG - Cache HIT L1: users:user:1
DEBUG - Cache HIT L2: users:user:2  
DEBUG - Cache MISS: users:user:3
INFO  - User retrieval took 2 ms
```

## Настройка производительности

### Caffeine (L1 Cache)

```yaml
cache:
  caffeine:
    spec: maximumSize=1000,expireAfterWrite=5m,recordStats
```

Параметры:
- `maximumSize=1000` - максимальное количество записей
- `expireAfterWrite=5m` - TTL после записи
- `expireAfterAccess=2m` - TTL после последнего доступа
- `recordStats` - включить сбор статистики

### Redis (L2 Cache)

```yaml
spring:
  data:
    redis:
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0

cache:
  redis:
    default-ttl: 600000 # 10 минут
    key-prefix: "cache:"
```

## Мониторинг через Redis CLI

```bash
# Подключение к Redis
redis-cli

# Просмотр всех ключей кеша
KEYS cache:*

# Получение значения
GET cache:users:user:1

# Информация о памяти
INFO memory

# Количество ключей
DBSIZE
```

## Веб-интерфейс Redis

После запуска `docker-compose up -d`:
- Redis Commander: http://localhost:8081
- Просмотр ключей, значений, статистики

## Расширенные сценарии

### 1. Условное кеширование

```java
// Кешировать только если результат не null
public User getUserById(Long id) {
    return cacheService.get(CACHE_NAME, "user:" + id, User.class, 
        () -> {
            User user = userRepository.findById(id);
            return user != null ? user : null; // null не кешируется
        });
}
```

### 2. Кеш warming

```java
@PostConstruct
public void warmCache() {
    // Предварительная загрузка популярных данных
    List<Long> popularUserIds = Arrays.asList(1L, 2L, 3L);
    popularUserIds.forEach(this::getUserById);
}
```

### 3. Кастомная TTL

```java
// Разные TTL для разных типов данных
public void putWithCustomTtl(String cacheName, String key, Object value, Duration ttl) {
    // Реализация с кастомным TTL
}
```

## Troubleshooting

### Проблема: Redis недоступен
**Решение**: Приложение продолжит работать с L1 кешем. Проверьте:
```bash
docker-compose ps
redis-cli ping
```

### Проблема: Высокое потребление памяти
**Решение**: Настройте размеры кешей:
```yaml
cache:
  caffeine:
    spec: maximumSize=500,expireAfterWrite=2m
```

### Проблема: Медленные запросы
**Решение**: Проверьте логи и настройки:
```bash
# Проверьте время выполнения в логах
grep "took.*ms" logs/application.log
```