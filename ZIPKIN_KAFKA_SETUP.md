# 🔍 Spring Boot + Kafka + Zipkin - Полная настройка распределенной трассировки

## 🎯 **Цель:**
Настроить Spring Boot приложение для работы с Kafka через Zipkin для распределенной трассировки сообщений.

## 🏗️ **Архитектура:**

```
Spring Boot App ←→ Kafka ←→ Zipkin
     ↓              ↓        ↓
  Traces        Messages   Storage
     ↓              ↓        ↓
  Logs         Tracing    UI (9411)
```

## 📦 **Компоненты:**

### 1. **Kafka** - Message Broker
- Порт: 9092
- JMX метрики: 9101
- UI: http://localhost:8081

### 2. **Zipkin** - Distributed Tracing
- Порт: 9411
- UI: http://localhost:9411
- Storage: Kafka (топик zipkin)

### 3. **Spring Boot** - Приложение с трассировкой
- Порт: 8080
- Health: http://localhost:8080/actuator/health
- API: http://localhost:8080/api/zipkin/*

## ⚙️ **Конфигурация:**

### 1. **Зависимости в pom.xml**
```xml
<!-- Spring Cloud Sleuth для трассировки -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>

<!-- Zipkin для распределенной трассировки -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
</dependency>

<!-- Brave для трассировки Kafka -->
<dependency>
    <groupId>io.zipkin.brave</groupId>
    <artifactId>brave-instrumentation-kafka-clients</artifactId>
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
  
  sleuth:
    sampler:
      probability: 1.0  # 100% трассировка
    messaging:
      kafka:
        enabled: true
    zipkin:
      base-url: http://localhost:9411
      sender:
        type: kafka
      kafka:
        topic: zipkin
```

### 3. **ZipkinConfig.java**
```java
@Configuration
@EnableKafka
public class ZipkinConfig {
    
    @Bean
    public ProducerFactory<String, String> tracingProducerFactory() {
        // Настройка Producer с трассировкой
        configProps.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, 
                       TracingProducerInterceptor.class.getName());
    }
    
    @Bean
    public ConsumerFactory<String, String> tracingConsumerFactory() {
        // Настройка Consumer с трассировкой
        configProps.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, 
                       TracingConsumerInterceptor.class.getName());
    }
}
```

## 🚀 **Запуск:**

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

## 📊 **Мониторинг:**

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

## 🔍 **Диагностика:**

### 1. **Проверка трассировки**
```bash
# Логи с trace ID
docker logs spring-app-zipkin | grep "TRACED"

# Zipkin API
curl http://localhost:9411/api/v2/traces

# Kafka топики
curl http://localhost:8081/api/clusters/local/topics
```

### 2. **Частые проблемы**

#### **Трассировка не работает**
```yaml
# Проверьте конфигурацию
spring:
  sleuth:
    sampler:
      probability: 1.0  # Должно быть > 0
    messaging:
      kafka:
        enabled: true   # Должно быть true
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
// Убедитесь, что используются правильные interceptors
configProps.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, 
               TracingProducerInterceptor.class.getName());
configProps.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, 
               TracingConsumerInterceptor.class.getName());
```

## 📈 **Производительность:**

### 1. **Настройка sampling**
```yaml
# Для production используйте меньший sampling rate
spring:
  sleuth:
    sampler:
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

### 🚀 **Запуск:**
```bash
./start-zipkin-kafka.sh
```

### 🔍 **Просмотр трассировок:**
- Zipkin UI: http://localhost:9411
- Kafka UI: http://localhost:8081
- Grafana: http://localhost:3000

Теперь у вас есть полная система распределенной трассировки Spring Boot + Kafka + Zipkin! 🎉