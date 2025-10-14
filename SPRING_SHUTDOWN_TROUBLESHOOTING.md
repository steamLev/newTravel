# 🔧 Диагностика выключения Spring сервиса в Docker

## 🔍 **Кто может вырубать Spring сервис:**

### 1. **Consul (Service Discovery)**
```bash
# Проверьте статус в Consul UI
curl http://localhost:8500/v1/agent/services
curl http://localhost:8500/v1/health/service/spring-app-consul

# Проверьте health checks
curl http://localhost:8500/v1/agent/checks
```

**Причины выключения:**
- ❌ Health check failed (не отвечает `/actuator/health`)
- ❌ Health check timeout (превышено время ожидания)
- ❌ Critical timeout (критическое время превышено)
- ❌ Service deregistration (автоматическая отмена регистрации)

### 2. **Docker/Orchestrator**
```bash
# Проверьте Docker health check
docker inspect spring-app-consul | grep -A 10 Health

# Проверьте логи контейнера
docker logs spring-app-consul

# Проверьте ресурсы
docker stats spring-app-consul
```

**Причины выключения:**
- ❌ OOM (Out of Memory) - превышен лимит памяти
- ❌ Health check failed - не проходит проверку здоровья
- ❌ Resource limits - превышены лимиты CPU/памяти
- ❌ Container restart policy

### 3. **RabbitMQ**
```bash
# Проверьте соединение
curl http://localhost:15672/api/connections

# Проверьте каналы
curl http://localhost:15672/api/channels
```

**Причины выключения:**
- ❌ Connection lost - потеряно соединение с RabbitMQ
- ❌ Channel errors - ошибки каналов
- ❌ Consumer errors - ошибки consumer'ов
- ❌ Heartbeat timeout - превышено время heartbeat

### 4. **Load Balancer/Proxy (Nginx)**
```bash
# Проверьте upstream статус
curl http://localhost/health

# Проверьте Nginx логи
docker logs nginx-lb
```

**Причины выключения:**
- ❌ Upstream unavailable - сервис недоступен
- ❌ Max fails exceeded - превышено количество неудач
- ❌ Health check failed - не проходит проверку

## 🛠️ **Диагностические команды:**

### 1. **Проверка статуса сервисов**
```bash
# Docker контейнеры
docker-compose -f docker-compose-consul.yml ps

# Consul сервисы
curl http://localhost:8500/v1/catalog/services

# Health checks
curl http://localhost:8500/v1/agent/checks
```

### 2. **Проверка логов**
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

### 3. **Проверка метрик**
```bash
# Prometheus targets
curl http://localhost:9090/api/v1/targets

# Spring Boot метрики
curl http://localhost:8080/actuator/metrics

# Health endpoint
curl http://localhost:8080/actuator/health
```

## 🔧 **Решения проблем:**

### 1. **Consul health check failed**
```yaml
# В application-consul.yml
spring:
  cloud:
    consul:
      discovery:
        health-check-interval: 10s
        health-check-timeout: 5s
        health-check-critical-timeout: 30s
        health-check-path: /actuator/health
```

### 2. **Docker OOM**
```yaml
# В docker-compose-consul.yml
services:
  spring-app:
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 256M
```

### 3. **RabbitMQ connection issues**
```yaml
# В application-consul.yml
spring:
  rabbitmq:
    connection-timeout: 10000
    requested-heartbeat: 30
    listener:
      simple:
        retry:
          enabled: true
          max-attempts: 3
```

### 4. **Graceful shutdown**
```yaml
# В application-consul.yml
server:
  shutdown: graceful
management:
  endpoint:
    shutdown:
      enabled: true
```

## 🚨 **Мониторинг и алерты:**

### 1. **Prometheus запросы**
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

### 2. **Grafana дашборд**
- Service availability
- Health check status
- Memory usage
- RabbitMQ connections
- Response times

### 3. **Алерты**
```yaml
# prometheus-alerts.yml
groups:
  - name: spring-app
    rules:
      - alert: SpringAppDown
        expr: up{job="spring-boot-app"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Spring application is down"
      
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.9
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage"
```

## 🧪 **Тестирование:**

### 1. **Health check тест**
```bash
# Проверка health endpoint
curl -f http://localhost:8080/actuator/health

# Проверка через Nginx
curl -f http://localhost/health
```

### 2. **Graceful shutdown тест**
```bash
# Инициация graceful shutdown
curl -X POST http://localhost:8080/api/lifecycle/shutdown

# Проверка статуса
curl http://localhost:8080/api/lifecycle/status
```

### 3. **Load test**
```bash
# Нагрузочное тестирование
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/messages/send/single \
    -H "Content-Type: application/json" \
    -d "{\"id\":\"test$i\",\"content\":\"Load test $i\",\"type\":\"test\"}" &
done
```

## 📝 **Чек-лист для диагностики:**

- [ ] Проверить Docker контейнеры: `docker ps`
- [ ] Проверить логи приложения: `docker logs spring-app-consul`
- [ ] Проверить Consul сервисы: `curl http://localhost:8500/v1/catalog/services`
- [ ] Проверить health checks: `curl http://localhost:8500/v1/agent/checks`
- [ ] Проверить RabbitMQ: `curl http://localhost:15672/api/connections`
- [ ] Проверить метрики: `curl http://localhost:8080/actuator/metrics`
- [ ] Проверить Prometheus targets: `http://localhost:9090/targets`
- [ ] Проверить Grafana дашборды: `http://localhost:3000`

## 🎯 **Профилактика:**

1. **Настройте правильные health checks**
2. **Установите лимиты ресурсов**
3. **Настройте graceful shutdown**
4. **Мониторьте метрики**
5. **Настройте алерты**
6. **Регулярно проверяйте логи**
7. **Тестируйте failover сценарии**