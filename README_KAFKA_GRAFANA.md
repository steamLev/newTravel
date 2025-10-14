# 🚀 Kafka + Grafana Мониторинг - Полное решение

## 🎯 **Проблема решена:**
Kafka топик не виден в Grafana из-за отсутствия правильной конфигурации мониторинга.

## ✅ **Что было создано:**

### 1. **Docker Compose с полным стеком мониторинга**
```bash
# Запуск всей инфраструктуры
./start-kafka-monitoring.sh
```

**Сервисы:**
- 🔌 **Kafka** (localhost:9092) с JMX метриками
- 🗄️ **Zookeeper** (localhost:2181)
- 🌐 **Kafka UI** (http://localhost:8080)
- 📊 **Prometheus** (http://localhost:9090)
- 📈 **Grafana** (http://localhost:3000)
- 📡 **JMX Exporter** для метрик Kafka

### 2. **Spring Boot конфигурация**
- ✅ Kafka зависимости в `pom.xml`
- ✅ Конфигурация в `application-kafka.yml`
- ✅ `@EnableKafka` в `KafkaConfig`
- ✅ Автоматическое создание топиков
- ✅ `@KafkaListener` для обработки сообщений
- ✅ Producer сервис для отправки сообщений
- ✅ Prometheus метрики включены

### 3. **API для тестирования**
```bash
# Тестовое сообщение
curl -X POST http://localhost:8080/api/kafka/test

# User event
curl -X POST "http://localhost:8080/api/kafka/user-event?userId=123&action=login"

# Order event  
curl -X POST "http://localhost:8080/api/kafka/order-event?orderId=456&status=created"

# Произвольное сообщение
curl -X POST "http://localhost:8080/api/kafka/send?topic=my-topic&message=Hello"
```

## 🔍 **Диагностика проблем:**

### 1. **Проверьте статус сервисов**
```bash
docker-compose -f docker-compose-kafka.yml ps
```

### 2. **Проверьте топики**
```bash
# В Kafka UI: http://localhost:8080
# Или через CLI:
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### 3. **Проверьте метрики в Prometheus**
```bash
# Откройте: http://localhost:9090/targets
# Убедитесь, что kafka-jmx target UP
```

### 4. **Проверьте Grafana**
```bash
# Откройте: http://localhost:3000
# Логин: admin/admin
# Prometheus уже подключен как источник данных
```

## 📊 **Grafana дашборды:**

### Готовые запросы для метрик:

**Количество сообщений в топике:**
```promql
kafka_topic_messages_in_per_sec{topic="test-topic"}
```

**Размер данных в топике:**
```promql
kafka_topic_bytes_in_per_sec{topic="test-topic"}
```

**Все топики:**
```promql
{__name__=~"kafka_topic_.*"}
```

**Consumer lag:**
```promql
kafka_consumer_lag_sum
```

## 🚨 **Частые проблемы и решения:**

### 1. **Топик не виден в Grafana**
- ✅ Убедитесь, что Kafka запущен с JMX
- ✅ Проверьте, что Prometheus собирает метрики
- ✅ Убедитесь, что Grafana подключен к Prometheus
- ✅ Отправьте сообщения в топик для активации метрик

### 2. **Метрики не появляются**
- ✅ Проверьте Prometheus targets: http://localhost:9090/targets
- ✅ Убедитесь, что JMX Exporter работает
- ✅ Проверьте, что топик активен (есть сообщения)

### 3. **Grafana не показывает данные**
- ✅ Проверьте подключение к Prometheus
- ✅ Убедитесь, что временной диапазон правильный
- ✅ Проверьте, что запросы PromQL корректны

## 🧪 **Тестирование:**

### 1. **Запустите инфраструктуру**
```bash
./start-kafka-monitoring.sh
```

### 2. **Запустите Spring Boot приложение**
```bash
mvn spring-boot:run --spring.profiles.active=kafka
```

### 3. **Отправьте тестовые сообщения**
```bash
curl -X POST http://localhost:8080/api/kafka/test
```

### 4. **Проверьте метрики**
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- Kafka UI: http://localhost:8080

## 📝 **Файлы конфигурации:**

- `docker-compose-kafka.yml` - Docker Compose с полным стеком
- `prometheus.yml` - Конфигурация Prometheus
- `grafana/provisioning/datasources/prometheus.yml` - Grafana datasource
- `application-kafka.yml` - Spring Boot Kafka конфигурация
- `KafkaConfig.java` - Java конфигурация Kafka
- `KafkaMessageListener.java` - Слушатели сообщений
- `KafkaProducerService.java` - Сервис отправки сообщений

## 🎉 **Результат:**
Теперь ваши Kafka топики будут видны в Grafana с полным мониторингом:
- 📊 Количество сообщений
- 📈 Размер данных
- ⏱️ Consumer lag
- 🔄 Throughput
- 📋 Список топиков
- 📊 Графики в реальном времени

**Все готово к использованию!** 🚀