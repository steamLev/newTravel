# RabbitMQ Message Sender

Spring Boot приложение для отправки списка сообщений в RabbitMQ с проверкой доступных каналов в брокере.

## Возможности

- ✅ Отправка списка сообщений в RabbitMQ
- ✅ Проверка доступности каналов в брокере
- ✅ Асинхронная отправка сообщений
- ✅ Retry механизм для надежности
- ✅ Мониторинг статуса каналов
- ✅ REST API для тестирования
- ✅ Полное покрытие тестами

## Технологии

- Java 17
- Spring Boot 3.2.0
- Spring AMQP (RabbitMQ)
- Maven
- Docker & Docker Compose
- JUnit 5
- Mockito

## Быстрый старт

### 1. Запуск RabbitMQ брокера

```bash
docker-compose up -d
```

### 2. Запуск приложения

```bash
mvn spring-boot:run
```

### 3. Проверка работы

Приложение будет доступно по адресу: http://localhost:8080

## API Endpoints

### Отправка сообщений

#### POST /api/messages/send
Отправляет список сообщений

```json
{
  "messages": [
    {
      "content": "Test message 1",
      "type": "test",
      "priority": "high"
    },
    {
      "content": "Test message 2", 
      "type": "test",
      "priority": "medium"
    }
  ],
  "routingKey": "message.routing.key"
}
```

#### POST /api/messages/send/single
Отправляет одно сообщение

```json
{
  "content": "Single test message",
  "type": "test", 
  "priority": "high"
}
```

### Мониторинг каналов

#### GET /api/messages/channels/status
Получает статус каналов

```json
{
  "available": true,
  "channelCount": 3,
  "maxChannels": 10,
  "status": "AVAILABLE",
  "lastChecked": 1703123456789
}
```

#### GET /api/messages/channels/available
Проверяет доступность каналов

```json
true
```

### Тестовые endpoints

#### POST /api/messages/test/create?count=5&type=test
Создает тестовые сообщения

#### POST /api/messages/test/send?count=5&type=test&routingKey=test.key
Отправляет тестовые сообщения

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

## Тестирование

### Запуск всех тестов

```bash
mvn test
```

### Запуск интеграционных тестов

```bash
mvn test -Dtest=*IntegrationTest
```

### Запуск с профилем test

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

## Мониторинг

- **Health Check**: http://localhost:8080/actuator/health
- **RabbitMQ Management UI**: http://localhost:15672 (guest/guest)

## Архитектура

### Основные компоненты

1. **MessageSenderService** - основной сервис для отправки сообщений
2. **ChannelMonitorService** - мониторинг доступности каналов
3. **MessageController** - REST API контроллер
4. **RabbitMQConfig** - конфигурация RabbitMQ

### Поток данных

1. Клиент отправляет запрос на `/api/messages/send`
2. Контроллер проверяет доступность каналов
3. Создается MessageBatch с сообщениями
4. MessageSenderService отправляет сообщения асинхронно
5. Каждое сообщение отправляется через RabbitTemplate
6. Результат возвращается клиенту

## Логирование

Приложение использует структурированное логирование с уровнями:

- `DEBUG` - детальная информация о работе каналов
- `INFO` - основные операции
- `WARN` - предупреждения о недоступности каналов
- `ERROR` - ошибки отправки сообщений

## Производительность

- Асинхронная отправка сообщений
- Пул потоков для параллельной обработки
- Retry механизм с экспоненциальной задержкой
- Мониторинг каналов в реальном времени

## Безопасность

- Publisher confirms для гарантии доставки
- Publisher returns для обработки недоставленных сообщений
- Валидация доступности каналов перед отправкой