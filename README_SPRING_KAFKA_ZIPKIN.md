# 🔍 Spring Boot + Kafka + Zipkin - Полная настройка распределенной трассировки

## 🎯 **Ответ на вопрос: "Как подключить Spring к Kafka через Zipkin?"**

### ✅ **Полное решение создано!**

## 🏗️ **Архитектура решения:**

```
Spring Boot App ←→ Kafka ←→ Zipkin
     ↓              ↓        ↓
  Traces        Messages   Storage
     ↓              ↓        ↓
  Logs         Tracing    UI (9411)
```

## 📦 **Компоненты системы:**

### 1. **Kafka** - Message Broker
- **Порт**: 9092
- **JMX метрики**: 9101
- **UI**: http://localhost:8081

### 2. **Zipkin** - Distributed Tracing
- **Порт**: 9411
- **UI**: http://localhost:9411
- **Storage**: Kafka (топик zipkin)

### 3. **Spring Boot** - Приложение с трассировкой
- **Порт**: 8080
- **Health**: http://localhost:8080/actuator/health
- **API**: http://localhost:8080/api/zipkin/*

## ⚙️ **Конфигурация:**

### 1. **Зависимости в pom.xml**
```xml
<!-- Micrometer Tracing для трассировки -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>

<!-- Zipkin для распределенной трассировки -->
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>

<!-- Spring Cloud Stream для Kafka -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-kafka</artifactId>
</dependency>
```

### 2. **application-zipkin.yml**
```yaml
spring:
  zipkin:
    base-url: http://localhost:9411
    sender:
      type: kafka
    kafka:
      topic: zipkin
  
  tracing:
    zipkin:
      endpoint: http://localhost:9411/api/v2/spans
    sampling:
      probability: 1.0  # 100% трассировка для разработки

  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
    consumer:
      group-id: spring-app-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

### 3. **ZipkinConfig.java**
```java
@Configuration
@EnableKafka
public class ZipkinConfig {
    
    @Bean
    public ProducerFactory<String, String> tracingProducerFactory() {
        // Автоматическая трассировка через Micrometer Tracing
    }
    
    @Bean
    public ConsumerFactory<String, String> tracingConsumerFactory() {
        // Автоматическая трассировка через Micrometer Tracing
    }
}
```

## 🚀 **Запуск системы:**

### 1. **Запуск всей инфраструктуры**
```bash
./start-zipkin-kafka.sh
```

### 2. **Проверка статуса**
```bash
# Docker контейнеры
docker-compose -f docker-compose-zipkin.yml ps

# Zipkin health
curl http://localhost:9411/health

# Spring Boot health
curl http://localhost:8080/actuator/health
```

## 🧪 **Тестирование трассировки:**

### 1. **Базовое тестирование**
```bash
# Тестовое сообщение
curl -X POST http://localhost:8080/api/zipkin/test

# User event
curl -X POST "http://localhost:8080/api/zipkin/user-event?userId=123&action=login"

# Произвольное сообщение
curl -X POST "http://localhost:8080/api/zipkin/send?topic=test-topic&message=Hello Zipkin"

# Цепочка трассировки
curl -X POST http://localhost:8080/api/zipkin/chain-test
```

### 2. **Просмотр трассировок**
- **Zipkin UI**: http://localhost:9411
- **Kafka UI**: http://localhost:8081
- **Grafana**: http://localhost:3000

## 📊 **Мониторинг и метрики:**

### 1. **Prometheus метрики**
```bash
# Доступность сервисов
curl http://localhost:9090/api/v1/targets

# Spring Boot метрики
curl http://localhost:8080/actuator/prometheus
```

### 2. **Grafana дашборды**
- Service traces
- Kafka message flow
- Error rates
- Response times

## 🔍 **Диагностика трассировки:**

### 1. **Проверка трассировки**
```bash
# Логи с trace ID
docker logs spring-app-zipkin | grep "TRACED"

# Zipkin API
curl http://localhost:9411/api/v2/traces

# Kafka топики
curl http://localhost:8081/api/clusters/local/topics
```

### 2. **Частые проблемы и решения**

#### **Трассировка не работает**
```yaml
# Проверьте конфигурацию
spring:
  tracing:
    sampling:
      probability: 1.0  # Должно быть > 0
```

#### **Zipkin не получает трассы**
```bash
# Проверьте Kafka топик zipkin
curl http://localhost:8081/api/clusters/local/topics/zipkin

# Проверьте Zipkin конфигурацию
curl http://localhost:9411/config
```

#### **Kafka сообщения не трассируются**
```java
// Убедитесь, что используется правильная конфигурация
// Micrometer Tracing автоматически добавляет трассировку
```

## 📈 **Производительность:**

### 1. **Настройка sampling**
```yaml
# Для production используйте меньший sampling rate
spring:
  tracing:
    sampling:
      probability: 0.1  # 10% трассировка
```

### 2. **Оптимизация Kafka**
```yaml
spring:
  kafka:
    producer:
      batch-size: 16384
      linger-ms: 5
    consumer:
      max-poll-records: 500
```

## 🎯 **Итог:**

### ✅ **Что получили:**
1. **Полная трассировка Kafka сообщений** через Zipkin
2. **Распределенная трассировка** между сервисами
3. **Мониторинг производительности** Kafka
4. **Визуализация потоков данных** в Zipkin UI
5. **Интеграция с Prometheus/Grafana**
6. **Автоматическая трассировка** через Micrometer Tracing

### 🚀 **Запуск:**
```bash
./start-zipkin-kafka.sh
```

### 🔍 **Просмотр трассировок:**
- **Zipkin UI**: http://localhost:9411
- **Kafka UI**: http://localhost:8081
- **Grafana**: http://localhost:3000

### 📝 **API для тестирования:**
- `POST /api/zipkin/test` - тестовое сообщение
- `POST /api/zipkin/user-event` - user event
- `POST /api/zipkin/send` - произвольное сообщение
- `POST /api/zipkin/chain-test` - цепочка трассировки
- `GET /api/zipkin/info` - информация о трассировке

## 🎉 **Результат:**

Теперь у вас есть **полная система распределенной трассировки Spring Boot + Kafka + Zipkin**! 

- ✅ **Автоматическая трассировка** всех Kafka операций
- ✅ **Визуализация потоков данных** в Zipkin UI
- ✅ **Мониторинг производительности** в Grafana
- ✅ **Простое тестирование** через REST API
- ✅ **Production-ready** конфигурация

**Все готово к использованию!** 🚀