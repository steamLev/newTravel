# Двухуровневый кеш на Caffeine и Redis для Spring Boot

Этот проект демонстрирует реализацию двухуровневого кеша с использованием Caffeine (L1 - локальный кеш) и Redis (L2 - распределенный кеш) в Spring Boot приложении.

## Архитектура

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│  L1 Cache   │───▶│  L2 Cache   │───▶│  Database   │
│             │    │ (Caffeine)  │    │   (Redis)   │    │             │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

- **L1 Cache (Caffeine)**: Быстрый локальный кеш в памяти приложения
- **L2 Cache (Redis)**: Распределенный кеш для обмена данными между экземплярами приложения
- **Database**: Источник данных (в примере - имитация в памяти)

## Особенности

- Автоматическое кеширование с fallback стратегией
- Настраиваемые TTL для каждого уровня кеша
- Логирование операций кеширования
- Обработка ошибок с graceful degradation
- JSON сериализация для Redis

## Требования

- Java 17+
- Maven 3.6+
- Redis Server

## Установка и запуск

1. **Установите Redis**:
   ```bash
   # Ubuntu/Debian
   sudo apt-get install redis-server
   
   # macOS
   brew install redis
   
   # Docker
   docker run -d -p 6379:6379 redis:alpine
   ```

2. **Запустите Redis**:
   ```bash
   redis-server
   ```

3. **Соберите и запустите приложение**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## API Endpoints

### Пользователи

- `GET /api/users` - Получить всех пользователей
- `GET /api/users/{id}` - Получить пользователя по ID
- `POST /api/users` - Создать нового пользователя
- `PUT /api/users/{id}` - Обновить пользователя
- `DELETE /api/users/{id}` - Удалить пользователя

### Управление кешем

- `POST /api/users/cache/clear` - Очистить кеш пользователей
- `GET /api/users/cache/stats` - Получить статистику кеша

## Примеры использования

### Получение пользователя (первый запрос - загрузка из БД)
```bash
curl http://localhost:8080/api/users/1
```

### Повторный запрос (загрузка из кеша)
```bash
curl http://localhost:8080/api/users/1
```

### Создание пользователя
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Новый",
    "lastName": "Пользователь",
    "email": "new@example.com"
  }'
```

## Конфигурация

### application.yml

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms

cache:
  caffeine:
    spec: maximumSize=1000,expireAfterWrite=5m,recordStats
  redis:
    default-ttl: 600000 # 10 минут
    key-prefix: "cache:"
```

### Параметры Caffeine

- `maximumSize=1000` - Максимальное количество записей
- `expireAfterWrite=5m` - TTL после записи (5 минут)
- `recordStats` - Включить сбор статистики

## Логирование

Включено подробное логирование операций кеширования:

```
DEBUG com.example.cache.service.TwoLevelCacheService - Cache HIT L1: users:user:1
DEBUG com.example.cache.service.TwoLevelCacheService - Cache HIT L2: users:user:2
DEBUG com.example.cache.service.TwoLevelCacheService - Cache MISS: users:user:3
```

## Мониторинг

### Redis CLI
```bash
redis-cli
> KEYS cache:*
> GET cache:users:user:1
```

### Caffeine статистика
Статистика Caffeine доступна через JMX (если включен `recordStats`).

## Производительность

- **L1 Cache (Caffeine)**: ~1-10 микросекунд
- **L2 Cache (Redis)**: ~1-5 миллисекунд
- **Database**: ~100+ миллисекунд (в примере)

## Расширение функциональности

1. **Добавление метрик**: Интеграция с Micrometer/Prometheus
2. **Кеш warming**: Предварительная загрузка популярных данных
3. **Кеш invalidation**: События для инвалидации кеша
4. **Conditional caching**: Условное кеширование на основе параметров
5. **Cache compression**: Сжатие данных в Redis

## Troubleshooting

### Redis недоступен
Приложение продолжит работать, используя только L1 кеш (Caffeine).

### Проблемы с сериализацией
Убедитесь, что модели данных реализуют `Serializable` и имеют конструкторы по умолчанию.

### Высокое потребление памяти
Настройте параметры Caffeine:
```yaml
cache:
  caffeine:
    spec: maximumSize=500,expireAfterWrite=2m
```