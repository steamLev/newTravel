# 📝 Создание топиков в Kafka - Полное решение

## 🎯 **Ответ на вопрос: "Как должен создаваться топик в Kafka?"**

### ✅ **Полное решение создано!**

## 🏗️ **Способы создания топиков:**

### 1. **Автоматическое создание через Spring Boot (Рекомендуется)**
### 2. **Ручное создание через Kafka Admin API**
### 3. **Создание через Kafka CLI**
### 4. **Создание через Kafka UI**

---

## 🚀 **1. Автоматическое создание через Spring Boot**

### **Конфигурация в KafkaConfig.java:**

```java
@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Включаем автоматическое создание топиков
        configs.put("auto.create.topics.enable", "true");
        return new KafkaAdmin(configs);
    }

    // Создание топика с базовыми настройками
    @Bean
    public NewTopic testTopic() {
        return TopicBuilder.name("test-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // Создание топика с расширенными настройками
    @Bean
    public NewTopic messagesTopic() {
        return TopicBuilder.name("messages-topic")
                .partitions(6)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 дней
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .build();
    }
}
```

### **Созданные топики:**

| Топик | Партиции | Реплики | Retention | Сжатие | Назначение |
|-------|----------|---------|-----------|--------|------------|
| `test-topic` | 3 | 1 | 7 дней | producer | Тестирование |
| `messages-topic` | 6 | 1 | 7 дней | snappy | Сообщения |
| `zipkin` | 3 | 1 | 1 день | gzip | Zipkin трассировки |
| `user-events` | 6 | 1 | 30 дней | snappy | Пользовательские события |
| `order-events` | 6 | 1 | 90 дней | snappy | Заказы |
| `system-events` | 3 | 1 | 7 дней | gzip | Системные события |
| `audit-events` | 12 | 1 | 1 год | gzip | Аудит |
| `application-logs` | 12 | 1 | 30 дней | snappy | Логи приложения |
| `metrics` | 6 | 1 | 7 дней | snappy | Метрики |

---

## 🛠️ **2. Ручное создание через Kafka Admin API**

### **KafkaTopicService.java:**

```java
@Service
public class KafkaTopicService {

    // Создание топика с базовыми настройками
    public void createTopic(String topicName, int partitions, int replicas) {
        // Реализация создания топика
    }

    // Создание топика для событий
    public void createEventsTopic(String topicName, int partitions) {
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "2592000000"); // 30 дней
        configs.put(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, "delete");
        configs.put(TopicConfig.SEGMENT_MS_CONFIG, "86400000"); // 1 день
        
        createTopicWithConfig(topicName, partitions, 1, configs);
    }

    // Создание топика для логов
    public void createLogsTopic(String topicName, int partitions) {
        // Конфигурация для логов
    }

    // Создание топика для аудита
    public void createAuditTopic(String topicName, int partitions) {
        // Конфигурация для аудита
    }
}
```

---

## 🌐 **3. REST API для управления топиками**

### **KafkaTopicController.java:**

```java
@RestController
@RequestMapping("/api/kafka/topics")
public class KafkaTopicController {

    // Создание топика
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createTopic(
            @RequestParam String topicName,
            @RequestParam(defaultValue = "3") int partitions,
            @RequestParam(defaultValue = "1") int replicas) {
        // Создание топика
    }

    // Создание топика для событий
    @PostMapping("/create/events")
    public ResponseEntity<Map<String, Object>> createEventsTopic(
            @RequestParam String topicName,
            @RequestParam(defaultValue = "6") int partitions) {
        // Создание топика для событий
    }

    // Создание топика для логов
    @PostMapping("/create/logs")
    public ResponseEntity<Map<String, Object>> createLogsTopic(
            @RequestParam String topicName,
            @RequestParam(defaultValue = "12") int partitions) {
        // Создание топика для логов
    }

    // Создание топика для аудита
    @PostMapping("/create/audit")
    public ResponseEntity<Map<String, Object>> createAuditTopic(
            @RequestParam String topicName,
            @RequestParam(defaultValue = "12") int partitions) {
        // Создание топика для аудита
    }

    // Список топиков
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listTopics() {
        // Получение списка топиков
    }

    // Описание топика
    @GetMapping("/describe/{topicName}")
    public ResponseEntity<Map<String, Object>> describeTopic(@PathVariable String topicName) {
        // Получение описания топика
    }

    // Удаление топика
    @DeleteMapping("/{topicName}")
    public ResponseEntity<Map<String, Object>> deleteTopic(@PathVariable String topicName) {
        // Удаление топика
    }

    // Статистика топиков
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTopicsStats() {
        // Получение статистики топиков
    }
}
```

---

## 💻 **4. Создание через Kafka CLI**

### **Базовые команды:**

```bash
# Создание простого топика
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic my-topic \
  --partitions 3 \
  --replication-factor 1

# Создание топика с конфигурацией
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic events-topic \
  --partitions 6 \
  --replication-factor 1 \
  --config retention.ms=604800000 \
  --config compression.type=snappy \
  --config cleanup.policy=delete

# Список топиков
kafka-topics.sh --list --bootstrap-server localhost:9092

# Описание топика
kafka-topics.sh --describe --bootstrap-server localhost:9092 --topic my-topic

# Удаление топика
kafka-topics.sh --delete --bootstrap-server localhost:9092 --topic my-topic
```

---

## 🧪 **Тестирование создания топиков:**

### **1. Через REST API:**

```bash
# Создание базового топика
curl -X POST "http://localhost:8080/api/kafka/topics/create?topicName=my-test-topic&partitions=3&replicas=1"

# Создание топика для событий
curl -X POST "http://localhost:8080/api/kafka/topics/create/events?topicName=user-events&partitions=6"

# Создание топика для логов
curl -X POST "http://localhost:8080/api/kafka/topics/create/logs?topicName=app-logs&partitions=12"

# Создание топика для аудита
curl -X POST "http://localhost:8080/api/kafka/topics/create/audit?topicName=audit-logs&partitions=12"

# Список топиков
curl http://localhost:8080/api/kafka/topics/list

# Описание топика
curl http://localhost:8080/api/kafka/topics/describe/my-test-topic

# Статистика топиков
curl http://localhost:8080/api/kafka/topics/stats

# Удаление топика
curl -X DELETE http://localhost:8080/api/kafka/topics/my-test-topic
```

### **2. Через Kafka UI:**
- **URL**: http://localhost:8081
- **Раздел**: Topics
- **Действие**: Add a Topic

---

## ⚙️ **Параметры конфигурации топиков:**

### **Основные параметры:**

| Параметр | Описание | По умолчанию | Рекомендуемое значение |
|----------|----------|--------------|----------------------|
| `num.partitions` | Количество партиций | 1 | 6-12 для большинства случаев |
| `replication.factor` | Фактор репликации | 1 | 3 для production |
| `retention.ms` | Время хранения сообщений | 604800000 (7 дней) | Зависит от типа данных |
| `segment.ms` | Время сегмента | 604800000 (7 дней) | 86400000 (1 день) |
| `compression.type` | Тип сжатия | producer | snappy для событий, gzip для логов |
| `cleanup.policy` | Политика очистки | delete | delete для большинства случаев |

### **Продвинутые параметры:**

| Параметр | Описание | Рекомендуемое значение |
|----------|----------|----------------------|
| `retention.bytes` | Максимальный размер топика | -1 (без ограничений) |
| `segment.bytes` | Размер сегмента | 1073741824 (1GB) |
| `max.message.bytes` | Максимальный размер сообщения | 1048576 (1MB) |
| `min.compaction.lag.ms` | Минимальная задержка компактификации | 0 |
| `delete.retention.ms` | Время хранения удаленных записей | 86400000 (1 день) |

---

## 🎯 **Рекомендации по созданию топиков:**

### **1. Количество партиций:**
- **Для высокой пропускной способности**: 6-12 партиций
- **Для событий**: 12-24 партиции
- **Для логов**: 24+ партиций
- **Правило**: Количество партиций = количество consumer'ов в группе

### **2. Фактор репликации:**
- **Development**: 1
- **Production**: 3 (минимум)
- **Critical systems**: 5+

### **3. Время хранения:**
- **Events**: 7-30 дней
- **Logs**: 30-90 дней
- **Metrics**: 1-7 дней
- **Audit**: 1+ год

### **4. Сжатие:**
- **Snappy**: Быстрое сжатие, средний коэффициент
- **Gzip**: Медленное сжатие, высокий коэффициент
- **LZ4**: Быстрое сжатие, низкий коэффициент
- **Zstd**: Современное сжатие, высокий коэффициент

---

## 🚀 **Запуск системы:**

### **1. Запуск всей инфраструктуры:**
```bash
./start-zipkin-kafka.sh
```

### **2. Проверка создания топиков:**
```bash
# Список топиков
curl http://localhost:8080/api/kafka/topics/list

# Статистика топиков
curl http://localhost:8080/api/kafka/topics/stats

# Kafka UI
open http://localhost:8081
```

---

## ✅ **Итог:**

### **Лучшие практики:**
1. **Используйте Spring Boot** для автоматического создания
2. **Настройте правильное количество партиций** (6-12 для большинства случаев)
3. **Используйте репликацию** (минимум 3 для production)
4. **Настройте retention** в зависимости от типа данных
5. **Используйте сжатие** для экономии места
6. **Мониторьте топики** через Kafka UI или Prometheus

### **Созданные компоненты:**
- ✅ **KafkaConfig.java** - автоматическое создание топиков
- ✅ **KafkaTopicService.java** - сервис для управления топиками
- ✅ **KafkaTopicController.java** - REST API для управления топиками
- ✅ **9 предустановленных топиков** с оптимальными настройками
- ✅ **Полная документация** по созданию топиков

### **Автоматическое создание - лучший выбор!** 🎉

**Теперь у вас есть полная система управления топиками Kafka!** 🚀