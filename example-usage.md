# Примеры использования RabbitMQ Message Sender

## Запуск приложения

### 1. Запуск RabbitMQ брокера

```bash
docker-compose up -d
```

### 2. Запуск Spring Boot приложения

```bash
mvn spring-boot:run
```

### 3. Проверка работы

```bash
./test-api.sh
```

## Примеры API вызовов

### Проверка статуса каналов

```bash
curl http://localhost:8080/api/messages/channels/status
```

Ответ:
```json
{
  "available": true,
  "channelCount": 0,
  "maxChannels": 10,
  "status": "AVAILABLE",
  "lastChecked": 1703123456789
}
```

### Отправка одного сообщения

```bash
curl -X POST http://localhost:8080/api/messages/send/single \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Тестовое сообщение",
    "type": "test",
    "priority": "high"
  }'
```

### Отправка списка сообщений

```bash
curl -X POST http://localhost:8080/api/messages/send \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {
        "content": "Сообщение 1",
        "type": "order",
        "priority": "high"
      },
      {
        "content": "Сообщение 2",
        "type": "notification", 
        "priority": "medium"
      }
    ],
    "routingKey": "orders.processing"
  }'
```

### Создание тестовых сообщений

```bash
curl "http://localhost:8080/api/messages/test/create?count=5&type=demo"
```

### Отправка тестовых сообщений

```bash
curl -X POST "http://localhost:8080/api/messages/test/send?count=5&type=demo&routingKey=demo.key"
```

## Мониторинг

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### RabbitMQ Management UI

Откройте http://localhost:15672 в браузере:
- Логин: guest
- Пароль: guest

## Логирование

Приложение выводит подробные логи о:
- Статусе каналов
- Отправке сообщений
- Ошибках подключения
- Retry попытках

## Конфигурация

Основные настройки в `application.yml`:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

rabbitmq:
  channel:
    max-available: 10
    check-interval: 5000
  queue:
    name: message.queue
  exchange:
    name: message.exchange
```

## Обработка ошибок

Приложение автоматически:
- Проверяет доступность каналов перед отправкой
- Повторяет отправку при ошибках (до 3 раз)
- Логирует все ошибки
- Возвращает понятные сообщения об ошибках

## Производительность

- Асинхронная отправка сообщений
- Параллельная обработка
- Мониторинг каналов в реальном времени
- Retry механизм с экспоненциальной задержкой