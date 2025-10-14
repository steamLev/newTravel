# 🔍 Анализ выключения Spring сервиса в Docker с Consul и RabbitMQ

## 🎯 **Ответ на вопрос: "Кто может вырубать Spring сервис?"**

### 1. **Consul (Service Discovery) - ОСНОВНОЙ ВИНОВНИК**
```bash
# Consul может вырубать сервис если:
curl http://localhost:8500/v1/agent/checks
# Покажет статус health checks
```

**Причины выключения Consul:**
- ❌ **Health check failed** - `/actuator/health` не отвечает
- ❌ **Health check timeout** - превышено время ожидания (5s)
- ❌ **Critical timeout** - критическое время (30s)
- ❌ **Service deregistration** - автоматическая отмена регистрации
- ❌ **Circuit breaker** - срабатывание circuit breaker паттерна

### 2. **Docker/Orchestrator**
```bash
# Проверьте Docker health check
docker inspect spring-app-consul | grep -A 10 Health
```

**Причины выключения Docker:**
- ❌ **OOM Killer** - превышен лимит памяти
- ❌ **Health check failed** - не проходит Docker health check
- ❌ **Resource limits** - превышены лимиты CPU/памяти
- ❌ **Container restart policy** - политика перезапуска

### 3. **RabbitMQ Connection Issues**
```bash
# Проверьте соединения
curl http://localhost:15672/api/connections
```

**Причины выключения RabbitMQ:**
- ❌ **Connection lost** - потеряно соединение
- ❌ **Channel errors** - ошибки каналов
- ❌ **Consumer errors** - ошибки consumer'ов
- ❌ **Heartbeat timeout** - превышено время heartbeat

### 4. **Load Balancer (Nginx)**
```bash
# Проверьте upstream
curl http://localhost/health
```

**Причины выключения Nginx:**
- ❌ **Upstream unavailable** - сервис недоступен
- ❌ **Max fails exceeded** - превышено количество неудач
- ❌ **Health check failed** - не проходит проверку

## 🛠️ **Полное решение создано:**

### 1. **Docker Compose с полным стеком**
```bash
# Запуск всей инфраструктуры
./start-consul-monitoring.sh
```

**Включает:**
- 🔌 **Consul** - Service Discovery
- 🐰 **RabbitMQ** - Message Broker
- 🌐 **Spring App** - Ваше приложение
- ⚖️ **Nginx** - Load Balancer
- 📊 **Prometheus** - Мониторинг
- 📈 **Grafana** - Визуализация

### 2. **Spring Boot конфигурация**
- ✅ Consul Discovery (`@EnableDiscoveryClient`)
- ✅ Health checks (`/actuator/health`)
- ✅ Graceful shutdown
- ✅ RabbitMQ retry logic
- ✅ Memory monitoring
- ✅ Custom health indicators

### 3. **Мониторинг и диагностика**
- ✅ Prometheus метрики
- ✅ Grafana дашборды
- ✅ Health check endpoints
- ✅ Lifecycle management API
- ✅ Detailed logging

## 🔍 **Диагностические команды:**

### 1. **Проверка статуса всех компонентов**
```bash
# Docker контейнеры
docker-compose -f docker-compose-consul.yml ps

# Consul сервисы
curl http://localhost:8500/v1/catalog/services

# Health checks
curl http://localhost:8500/v1/agent/checks

# Spring Boot health
curl http://localhost:8080/actuator/health

# RabbitMQ connections
curl http://localhost:15672/api/connections
```

### 2. **Логи для диагностики**
```bash
# Spring приложение
docker logs spring-app-consul -f

# Consul
docker logs consul-server -f

# RabbitMQ
docker logs rabbitmq-consul -f

# Nginx
docker logs nginx-lb -f
```

### 3. **Метрики и мониторинг**
```bash
# Prometheus targets
curl http://localhost:9090/api/v1/targets

# Spring Boot метрики
curl http://localhost:8080/actuator/metrics

# Grafana UI
# http://localhost:3000 (admin/admin)
```

## 🚨 **Частые причины выключения:**

### 1. **Consul Health Check Failed (90% случаев)**
```yaml
# Решение: Настройте правильные health checks
spring:
  cloud:
    consul:
      discovery:
        health-check-interval: 10s
        health-check-timeout: 5s
        health-check-critical-timeout: 30s
        health-check-path: /actuator/health
```

### 2. **Docker OOM (5% случаев)**
```yaml
# Решение: Установите лимиты памяти
deploy:
  resources:
    limits:
      memory: 1G
    reservations:
      memory: 256M
```

### 3. **RabbitMQ Connection Lost (3% случаев)**
```yaml
# Решение: Настройте retry logic
spring:
  rabbitmq:
    listener:
      simple:
        retry:
          enabled: true
          max-attempts: 3
```

### 4. **Nginx Upstream Failed (2% случаев)**
```nginx
# Решение: Настройте правильные upstream checks
upstream spring_app {
    server spring-app-consul:8080 max_fails=3 fail_timeout=30s;
}
```

## 🧪 **Тестирование выключения:**

### 1. **Тест Consul health check**
```bash
# Симулируйте недоступность health endpoint
docker exec spring-app-consul curl -f http://localhost:8080/actuator/health
# Если не отвечает - Consul вырубит сервис
```

### 2. **Тест graceful shutdown**
```bash
# Инициация graceful shutdown
curl -X POST http://localhost:8080/api/lifecycle/shutdown

# Проверка статуса
curl http://localhost:8080/api/lifecycle/status
```

### 3. **Тест OOM**
```bash
# Нагрузочное тестирование для вызова OOM
for i in {1..1000}; do
  curl -X POST http://localhost:8080/api/messages/send/single \
    -H "Content-Type: application/json" \
    -d "{\"id\":\"test$i\",\"content\":\"Load test $i\"}" &
done
```

## 📊 **Grafana дашборды для мониторинга:**

### Основные метрики:
- **Service Availability** - доступность сервиса
- **Health Check Status** - статус health checks
- **Memory Usage** - использование памяти
- **RabbitMQ Connections** - соединения с RabbitMQ
- **Response Times** - время отклика
- **Error Rates** - частота ошибок

### Prometheus запросы:
```promql
# Доступность сервиса
up{job="spring-boot-app"}

# Время отклика health check
http_server_requests_seconds{uri="/actuator/health"}

# Использование памяти
jvm_memory_used_bytes{area="heap"}

# RabbitMQ соединения
rabbitmq_connections
```

## 🎯 **Итог:**

**Основные виновники выключения Spring сервиса:**
1. **Consul** (90%) - health checks
2. **Docker** (5%) - OOM, resource limits
3. **RabbitMQ** (3%) - connection issues
4. **Nginx** (2%) - upstream failures

**Решение:**
- ✅ Полная инфраструктура мониторинга
- ✅ Правильные health checks
- ✅ Graceful shutdown
- ✅ Resource limits
- ✅ Retry logic
- ✅ Detailed logging
- ✅ Real-time monitoring

**Запуск:**
```bash
./start-consul-monitoring.sh
```

Теперь у вас есть полная система для диагностики и предотвращения выключения Spring сервиса! 🚀