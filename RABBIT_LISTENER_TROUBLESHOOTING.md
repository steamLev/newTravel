# 🔧 Устранение проблем с @RabbitListener

## ❌ **Проблема:**
`@RabbitListener` не создает consumer для очереди, хотя слушатель включен.

## 🔍 **Причины:**

### 1. **Отсутствует `@EnableRabbit`**
```java
// ❌ БЕЗ этой аннотации @RabbitListener НЕ РАБОТАЕТ
@Configuration
public class RabbitMQConfig {
    // ...
}

// ✅ ПРАВИЛЬНО
@Configuration
@EnableRabbit  // ← КРИТИЧЕСКИ ВАЖНО!
public class RabbitMQConfig {
    // ...
}
```

### 2. **Отсутствуют константы**
```java
// ❌ Константы не определены
@RabbitListener(queues = ConstantsRMQ.BINARY_QUEUE) // ← Ошибка!

// ✅ Создайте файл ConstantsRMQ.java
public class ConstantsRMQ {
    public static final String BINARY_QUEUE = "binary.queue";
    // ...
}
```

### 3. **Очередь не создана в конфигурации**
```java
// ❌ Очередь не объявлена как Bean
@RabbitListener(queues = "binary.queue") // ← Очередь не существует!

// ✅ Создайте Bean для очереди
@Bean
public Queue binaryQueue() {
    return QueueBuilder.durable(ConstantsRMQ.BINARY_QUEUE).build();
}
```

### 4. **Отключено автоматическое подключение**
```yaml
# ❌ НЕПРАВИЛЬНО
spring:
  rabbitmq:
    listener:
      simple:
        auto-startup: false  # ← Отключает @RabbitListener!

# ✅ ПРАВИЛЬНО
spring:
  rabbitmq:
    listener:
      simple:
        auto-startup: true   # ← Включает @RabbitListener!
        concurrency: 2
        max-concurrency: 10
```

### 5. **RabbitMQ сервер не запущен**
```bash
# Проверьте статус
curl http://localhost:8080/actuator/health

# Если RabbitMQ недоступен:
# 1. Запустите через Docker:
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# 2. Или установите локально:
sudo apt install rabbitmq-server
sudo systemctl start rabbitmq-server
```

## ✅ **Полное решение:**

### 1. **Добавьте `@EnableRabbit`**
```java
@Configuration
@EnableRabbit  // ← ОБЯЗАТЕЛЬНО!
public class RabbitMQConfig {
    // ...
}
```

### 2. **Создайте константы**
```java
public class ConstantsRMQ {
    public static final String BINARY_QUEUE = "binary.queue";
    public static final String BINARY_EXCHANGE = "binary.exchange";
    public static final String BINARY_ROUTING_KEY = "binary.routing.key";
}
```

### 3. **Создайте очередь и exchange**
```java
@Bean
public Queue binaryQueue() {
    return QueueBuilder.durable(ConstantsRMQ.BINARY_QUEUE).build();
}

@Bean
public DirectExchange binaryExchange() {
    return new DirectExchange(ConstantsRMQ.BINARY_EXCHANGE, true, false);
}

@Bean
public Binding binaryBinding() {
    return BindingBuilder
            .bind(binaryQueue())
            .to(binaryExchange())
            .with(ConstantsRMQ.BINARY_ROUTING_KEY);
}
```

### 4. **Создайте слушатель**
```java
@Component
public class DocumentConversionListener {

    @RabbitListener(queues = ConstantsRMQ.BINARY_QUEUE, concurrency = "2")
    public byte[] convertToPdfListener(byte[] file) {
        // Ваша логика обработки
        return conversionService.convertDocumentToPdf(file);
    }
}
```

### 5. **Включите автоматическое подключение**
```yaml
spring:
  rabbitmq:
    listener:
      simple:
        auto-startup: true
        concurrency: 2
        max-concurrency: 10
```

## 🧪 **Тестирование:**

### 1. **Проверьте health endpoint**
```bash
curl http://localhost:8080/actuator/health
# Должен показать: "rabbit": {"status": "UP"}
```

### 2. **Отправьте тестовое сообщение**
```bash
curl -X POST http://localhost:8080/api/test/rabbit-listener
```

### 3. **Проверьте логи**
```bash
# В логах должно быть:
# "Started RabbitListenerContainer"
# "Получен файл для конвертации в PDF"
```

## 🔍 **Диагностика:**

### 1. **Проверьте, что consumer создан**
```bash
# В RabbitMQ Management UI (http://localhost:15672)
# Перейдите в Queues → binary.queue
# Должен быть 1 consumer
```

### 2. **Проверьте логи приложения**
```bash
# Ищите в логах:
# "Started RabbitListenerContainer"
# "Declaring queue: binary.queue"
# "Declaring exchange: binary.exchange"
```

### 3. **Проверьте конфигурацию**
```bash
# Проверьте, что все Bean'ы созданы:
curl http://localhost:8080/actuator/beans | grep -i rabbit
```

## 🚨 **Частые ошибки:**

1. **Забыли `@EnableRabbit`** - самая частая причина
2. **Очередь не объявлена как Bean** - @RabbitListener не может найти очередь
3. **auto-startup: false** - отключает автоматическое создание consumer'ов
4. **RabbitMQ не запущен** - Connection refused
5. **Неправильное имя очереди** - опечатка в константах

## 📝 **Итог:**
Для работы `@RabbitListener` нужно:
- ✅ `@EnableRabbit` в конфигурации
- ✅ Очередь объявлена как Bean
- ✅ `auto-startup: true`
- ✅ RabbitMQ сервер запущен
- ✅ Правильные константы