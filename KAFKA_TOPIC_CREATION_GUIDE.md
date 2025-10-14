# 📝 Создание топиков в Kafka - Полное руководство

## 🎯 **Способы создания топиков в Kafka:**

### 1. **Автоматическое создание через Spring Boot (Рекомендуется)**
### 2. **Ручное создание через Kafka Admin API**
### 3. **Создание через Kafka CLI**
### 4. **Создание через Kafka UI**

---

## 🚀 **1. Автоматическое создание через Spring Boot**

### **Конфигурация в Java:**

```java
@Configuration
@EnableKafka
public class KafkaTopicConfig {

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        return new KafkaAdmin(configs);
    }

    // Создание топика с базовыми настройками
    @Bean
    public NewTopic basicTopic() {
        return TopicBuilder.name("basic-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // Создание топика с расширенными настройками
    @Bean
    public NewTopic advancedTopic() {
        return TopicBuilder.name("advanced-topic")
                .partitions(6)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 дней
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .build();
    }

    // Создание топика для событий
    @Bean
    public NewTopic eventsTopic() {
        return TopicBuilder.name("events-topic")
                .partitions(12)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000") // 30 дней
                .config(TopicConfig.SEGMENT_MS_CONFIG, "86400000") // 1 день
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .build();
    }
}
```

### **Конфигурация в YAML:**

```yaml
spring:
  kafka:
    admin:
      properties:
        bootstrap.servers: localhost:9092
    # Автоматическое создание топиков
    admin:
      auto-create: true
      # Настройки по умолчанию для новых топиков
      default-topic-configs:
        num-partitions: 3
        replication-factor: 1
        retention-ms: 604800000  # 7 дней
        compression-type: snappy
```

---

## 🛠️ **2. Ручное создание через Kafka Admin API**

### **Создание топика программно:**

```java
@Service
@RequiredArgsConstructor
public class KafkaTopicService {

    private final KafkaAdmin kafkaAdmin;

    public void createTopic(String topicName, int partitions, int replicas) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            
            // Проверяем, существует ли топик
            if (topicExists(adminClient, topicName)) {
                log.warn("Топик {} уже существует", topicName);
                return;
            }

            // Создаем топик
            NewTopic newTopic = TopicBuilder.name(topicName)
                    .partitions(partitions)
                    .replicas(replicas)
                    .build();

            CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
            
            // Ждем завершения создания
            result.all().get(30, TimeUnit.SECONDS);
            log.info("Топик {} успешно создан с {} партициями и {} репликами", 
                    topicName, partitions, replicas);
                    
        } catch (Exception e) {
            log.error("Ошибка при создании топика {}: {}", topicName, e.getMessage(), e);
            throw new RuntimeException("Failed to create topic", e);
        }
    }

    public void createTopicWithConfig(String topicName, int partitions, int replicas, 
                                    Map<String, String> configs) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            
            NewTopic newTopic = TopicBuilder.name(topicName)
                    .partitions(partitions)
                    .replicas(replicas)
                    .configs(configs)
                    .build();

            CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
            result.all().get(30, TimeUnit.SECONDS);
            
            log.info("Топик {} создан с конфигурацией: {}", topicName, configs);
        } catch (Exception e) {
            log.error("Ошибка при создании топика {}: {}", topicName, e.getMessage(), e);
            throw new RuntimeException("Failed to create topic", e);
        }
    }

    private boolean topicExists(AdminClient adminClient, String topicName) throws Exception {
        ListTopicsResult topics = adminClient.listTopics();
        return topics.names().get().contains(topicName);
    }

    public List<String> listTopics() throws Exception {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            ListTopicsResult topics = adminClient.listTopics();
            return new ArrayList<>(topics.names().get());
        }
    }

    public void deleteTopic(String topicName) throws Exception {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DeleteTopicsResult result = adminClient.deleteTopics(Collections.singletonList(topicName));
            result.all().get(30, TimeUnit.SECONDS);
            log.info("Топик {} удален", topicName);
        }
    }
}
```

---

## 💻 **3. Создание через Kafka CLI**

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

# Создание топика для логов
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic logs-topic \
  --partitions 12 \
  --replication-factor 1 \
  --config retention.ms=2592000000 \
  --config segment.ms=86400000 \
  --config cleanup.policy=delete
```

### **Управление топиками:**

```bash
# Список топиков
kafka-topics.sh --list --bootstrap-server localhost:9092

# Описание топика
kafka-topics.sh --describe --bootstrap-server localhost:9092 --topic my-topic

# Изменение конфигурации топика
kafka-configs.sh --bootstrap-server localhost:9092 \
  --entity-type topics --entity-name my-topic \
  --alter --add-config retention.ms=604800000

# Удаление топика
kafka-topics.sh --delete --bootstrap-server localhost:9092 --topic my-topic
```

---

## 🌐 **4. Создание через Kafka UI**

### **Kafka UI (http://localhost:8081):**

1. **Перейти в раздел "Topics"**
2. **Нажать "Add a Topic"**
3. **Заполнить параметры:**
   - Topic name
   - Number of partitions
   - Replication factor
   - Configuration (опционально)
4. **Нажать "Create Topic"**

---

## ⚙️ **Параметры конфигурации топиков:**

### **Основные параметры:**

| Параметр | Описание | По умолчанию |
|----------|----------|--------------|
| `num.partitions` | Количество партиций | 1 |
| `replication.factor` | Фактор репликации | 1 |
| `retention.ms` | Время хранения сообщений | 604800000 (7 дней) |
| `segment.ms` | Время сегмента | 604800000 (7 дней) |
| `compression.type` | Тип сжатия | producer |
| `cleanup.policy` | Политика очистки | delete |
| `min.insync.replicas` | Минимум синхронных реплик | 1 |

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

## 🚀 **Практический пример:**

### **Создание топика для микросервиса:**

```java
@Bean
public NewTopic userServiceTopic() {
    return TopicBuilder.name("user-service-events")
            .partitions(6)                    // 6 партиций для высокой пропускной способности
            .replicas(3)                      // 3 реплики для отказоустойчивости
            .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000")  // 30 дней
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")  // Быстрое сжатие
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")    // Удаление старых сообщений
            .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")   // Минимум 2 синхронные реплики
            .build();
}
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

### **Автоматическое создание - лучший выбор!** 🎉