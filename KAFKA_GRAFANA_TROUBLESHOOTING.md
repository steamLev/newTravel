# 🔧 Устранение проблем с Kafka топиками в Grafana

## ❌ **Проблема:**
Kafka топик не виден в Grafana, хотя настройки есть.

## 🔍 **Возможные причины:**

### 1. **Kafka не запущен**
```bash
# Проверьте статус Kafka
docker ps | grep kafka
# или
kafka-topics --bootstrap-server localhost:9092 --list
```

### 2. **Неправильная конфигурация подключения**
```yaml
# ❌ НЕПРАВИЛЬНО
spring:
  kafka:
    bootstrap-servers: kafka:9092  # Недоступно из Grafana

# ✅ ПРАВИЛЬНО
spring:
  kafka:
    bootstrap-servers: localhost:9092  # Доступно из Grafana
```

### 3. **Отсутствуют JMX метрики**
```yaml
# ✅ Включите JMX метрики
environment:
  KAFKA_JMX_PORT: 9101
  KAFKA_JMX_OPTS: -Dcom.sun.management.jmxremote
```

### 4. **Prometheus не собирает метрики Kafka**
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'kafka-jmx'
    static_configs:
      - targets: ['kafka-jmx-exporter:9404']
```

### 5. **Grafana не подключен к Prometheus**
```yaml
# grafana/provisioning/datasources/prometheus.yml
datasources:
  - name: Prometheus
    type: prometheus
    url: http://prometheus:9090
```

## ✅ **Полное решение:**

### 1. **Запустите Kafka с мониторингом**
```bash
./start-kafka-monitoring.sh
```

### 2. **Проверьте, что топики созданы**
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

### 4. **Настройте Grafana**
```bash
# Откройте: http://localhost:3000
# Логин: admin/admin
# Добавьте Prometheus как источник данных
```

### 5. **Создайте дашборд в Grafana**

#### Запросы для метрик Kafka:

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

## 🧪 **Тестирование:**

### 1. **Отправьте тестовые сообщения**
```bash
# Тестовое сообщение
curl -X POST http://localhost:8080/api/kafka/test

# User event
curl -X POST "http://localhost:8080/api/kafka/user-event?userId=123&action=login"

# Order event
curl -X POST "http://localhost:8080/api/kafka/order-event?orderId=456&status=created"
```

### 2. **Проверьте логи приложения**
```bash
# В логах должно быть:
# "📨 Получено сообщение из топика 'test-topic'"
# "📤 Отправляем сообщение в топик 'test-topic'"
```

### 3. **Проверьте метрики**
```bash
# Prometheus: http://localhost:9090
# Поиск: kafka_topic_messages_in_per_sec
```

## 🔍 **Диагностика:**

### 1. **Проверьте статус сервисов**
```bash
docker-compose -f docker-compose-kafka.yml ps
```

### 2. **Проверьте логи Kafka**
```bash
docker logs kafka
```

### 3. **Проверьте JMX метрики**
```bash
# Проверьте, что JMX порт открыт
netstat -tlnp | grep 9101
```

### 4. **Проверьте подключение Prometheus к Kafka**
```bash
# В Prometheus UI: http://localhost:9090/targets
# Статус kafka-jmx должен быть UP
```

## 📊 **Grafana дашборд для Kafka:**

### Импортируйте готовый дашборд:
1. Откройте Grafana: http://localhost:3000
2. Перейдите в "Import Dashboard"
3. Используйте ID: 721 (Kafka Overview)
4. Или создайте свой с запросами выше

### Основные метрики для мониторинга:
- **Messages In/Out per second** - количество сообщений
- **Bytes In/Out per second** - размер данных
- **Consumer Lag** - отставание consumer'ов
- **Partition Count** - количество партиций
- **Replication Factor** - фактор репликации

## 🚨 **Частые ошибки:**

1. **Kafka не запущен** - проверьте `docker ps`
2. **Неправильный bootstrap-servers** - используйте `localhost:9092`
3. **JMX не включен** - добавьте JMX настройки
4. **Prometheus не собирает метрики** - проверьте targets
5. **Grafana не подключен к Prometheus** - проверьте datasource
6. **Топик не создан** - создайте топик вручную или через код

## 📝 **Итог:**
Для видимости Kafka топиков в Grafana нужно:
- ✅ Kafka запущен с JMX метриками
- ✅ Prometheus собирает метрики Kafka
- ✅ Grafana подключен к Prometheus
- ✅ Топики созданы и активны
- ✅ Приложение отправляет/получает сообщения