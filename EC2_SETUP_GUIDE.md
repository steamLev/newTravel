# Руководство по деплою на Amazon EC2

## 🎯 Обзор

Это руководство поможет вам развернуть двухуровневый кеш на Amazon EC2 instance с использованием Docker и Docker Compose.

## 📋 Предварительные требования

### 1. Amazon EC2 Instance
- **OS**: Amazon Linux 2 или Ubuntu 20.04+
- **Instance Type**: t3.medium или больше (рекомендуется t3.large)
- **Storage**: минимум 20 GB
- **Security Groups**: открыть порты 22, 80, 8080, 8081

### 2. Локальная машина
- SSH клиент
- Docker (опционально, для тестирования)
- Git

## 🚀 Быстрый деплой

### 1. Подготовка EC2 Instance

```bash
# 1. Создайте EC2 instance
# - Выберите Amazon Linux 2 AMI
# - Instance Type: t3.medium
# - Security Group: откройте порты 22, 80, 8080, 8081
# - Создайте или выберите SSH ключ

# 2. Получите публичный IP
# - Запишите Public IPv4 address
# - Убедитесь что SSH ключ доступен локально
```

### 2. Автоматический деплой

```bash
# Клонируйте репозиторий (если нужно)
git clone <your-repo-url>
cd two-level-cache

# Запустите скрипт деплоя
./deploy-ec2.sh <EC2_IP> <SSH_KEY_PATH>

# Пример:
./deploy-ec2.sh 3.15.123.45 ~/.ssh/my-key.pem
```

### 3. Проверка деплоя

```bash
# Проверьте статус приложения
curl http://<EC2_IP>:8080/actuator/health

# Проверьте API
curl http://<EC2_IP>:8080/api/routed-cache/info

# Проверьте Redis Commander
# Откройте в браузере: http://<EC2_IP>:8081
```

## 🔧 Ручная настройка

### 1. Подключение к EC2

```bash
ssh -i ~/.ssh/your-key.pem ec2-user@<EC2_IP>
```

### 2. Установка Docker

```bash
# Amazon Linux 2
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user

# Ubuntu
sudo apt update
sudo apt install -y docker.io
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ubuntu
```

### 3. Установка Docker Compose

```bash
# Скачиваем последнюю версию
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# Делаем исполняемым
sudo chmod +x /usr/local/bin/docker-compose

# Проверяем установку
docker-compose --version
```

### 4. Копирование проекта

```bash
# Создаем директорию
mkdir -p ~/apps/two-level-cache
cd ~/apps/two-level-cache

# Копируем файлы (с локальной машины)
scp -i ~/.ssh/your-key.pem -r . ec2-user@<EC2_IP>:~/apps/two-level-cache/

# Или клонируем из Git
git clone <your-repo-url> .
```

### 5. Запуск приложения

```bash
# Переходим в директорию проекта
cd ~/apps/two-level-cache

# Запускаем в production режиме
docker-compose -f docker-compose.prod.yml up -d

# Проверяем статус
docker-compose -f docker-compose.prod.yml ps

# Смотрим логи
docker-compose -f docker-compose.prod.yml logs -f
```

## 🔧 Конфигурация

### 1. Security Groups

Убедитесь что в Security Groups открыты следующие порты:

| Port | Protocol | Source | Description |
|------|----------|--------|-------------|
| 22 | TCP | 0.0.0.0/0 | SSH |
| 80 | TCP | 0.0.0.0/0 | HTTP |
| 8080 | TCP | 0.0.0.0/0 | Application |
| 8081 | TCP | 0.0.0.0/0 | Redis Commander |

### 2. Environment Variables

Создайте файл `.env` для настройки:

```bash
# .env
SPRING_PROFILES_ACTIVE=integrated
SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379
TTL_REDIS=3600
JAVA_OPTS=-Xms512m -Xmx1024m
```

### 3. Nginx Configuration

Если используете Nginx, настройте SSL:

```bash
# Создайте директорию для SSL сертификатов
mkdir -p ~/apps/two-level-cache/ssl

# Скопируйте ваши SSL сертификаты
# ssl/cert.pem - сертификат
# ssl/key.pem - приватный ключ
```

## 📊 Мониторинг

### 1. Проверка статуса

```bash
# Статус контейнеров
docker-compose -f docker-compose.prod.yml ps

# Логи приложения
docker-compose -f docker-compose.prod.yml logs app

# Логи Redis
docker-compose -f docker-compose.prod.yml logs redis

# Использование ресурсов
docker stats
```

### 2. Health Checks

```bash
# Проверка здоровья приложения
curl http://<EC2_IP>:8080/actuator/health

# Проверка Redis
curl http://<EC2_IP>:8080/api/routed-cache/aml-client/cache/health

# Проверка статистики кеша
curl http://<EC2_IP>:8080/api/routed-cache/aml-client/cache/stats
```

### 3. Redis Commander

Откройте в браузере: `http://<EC2_IP>:8081`

- Просмотр ключей Redis
- Мониторинг памяти
- Выполнение команд Redis

## 🔄 Управление приложением

### 1. Остановка

```bash
docker-compose -f docker-compose.prod.yml down
```

### 2. Перезапуск

```bash
docker-compose -f docker-compose.prod.yml restart
```

### 3. Обновление

```bash
# Остановить приложение
docker-compose -f docker-compose.prod.yml down

# Обновить код
git pull

# Пересобрать и запустить
docker-compose -f docker-compose.prod.yml up --build -d
```

### 4. Очистка

```bash
# Остановить и удалить контейнеры
docker-compose -f docker-compose.prod.yml down

# Удалить неиспользуемые образы
docker image prune -f

# Удалить неиспользуемые volumes
docker volume prune -f
```

## 🚨 Troubleshooting

### 1. Приложение не запускается

```bash
# Проверьте логи
docker-compose -f docker-compose.prod.yml logs app

# Проверьте доступность Redis
docker-compose -f docker-compose.prod.yml logs redis

# Проверьте порты
netstat -tlnp | grep :8080
```

### 2. Redis недоступен

```bash
# Проверьте статус Redis
docker-compose -f docker-compose.prod.yml ps redis

# Перезапустите Redis
docker-compose -f docker-compose.prod.yml restart redis

# Проверьте подключение
docker exec -it two-level-cache-redis redis-cli ping
```

### 3. Высокое потребление памяти

```bash
# Проверьте использование памяти
docker stats

# Ограничьте память в docker-compose.prod.yml
# Добавьте в сервис app:
# mem_limit: 1g
```

### 4. Медленные запросы

```bash
# Проверьте логи приложения
docker-compose -f docker-compose.prod.yml logs app | grep "took"

# Проверьте статистику кеша
curl http://<EC2_IP>:8080/api/routed-cache/aml-client/cache/stats
```

## 📈 Оптимизация производительности

### 1. Настройка JVM

```bash
# В docker-compose.prod.yml добавьте:
environment:
  - JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC -XX:+UseContainerSupport
```

### 2. Настройка Redis

```bash
# В docker-compose.prod.yml добавьте в Redis:
command: redis-server --appendonly yes --maxmemory 512mb --maxmemory-policy allkeys-lru
```

### 3. Настройка Nginx

```bash
# Добавьте в nginx.conf:
worker_processes auto;
worker_connections 1024;
```

## 🔒 Безопасность

### 1. Firewall

```bash
# Настройте iptables (если нужно)
sudo iptables -A INPUT -p tcp --dport 22 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 80 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 8081 -j ACCEPT
sudo iptables -A INPUT -j DROP
```

### 2. SSL/TLS

```bash
# Используйте Let's Encrypt для SSL сертификатов
sudo yum install -y certbot
sudo certbot certonly --standalone -d your-domain.com
```

### 3. Мониторинг безопасности

```bash
# Установите fail2ban
sudo yum install -y fail2ban
sudo systemctl start fail2ban
sudo systemctl enable fail2ban
```

## 📚 Полезные команды

### Docker

```bash
# Просмотр всех контейнеров
docker ps -a

# Просмотр образов
docker images

# Очистка системы
docker system prune -a

# Просмотр логов
docker logs <container_name>
```

### Docker Compose

```bash
# Запуск в фоне
docker-compose -f docker-compose.prod.yml up -d

# Просмотр логов
docker-compose -f docker-compose.prod.yml logs -f

# Масштабирование
docker-compose -f docker-compose.prod.yml up --scale app=3 -d
```

### Система

```bash
# Использование диска
df -h

# Использование памяти
free -h

# Процессы
htop

# Сетевые соединения
netstat -tlnp
```

## 🎉 Готово!

После выполнения всех шагов у вас будет:

- ✅ Приложение доступно по адресу: `http://<EC2_IP>:8080`
- ✅ API документация: `http://<EC2_IP>:8080/api/routed-cache/info`
- ✅ Redis Commander: `http://<EC2_IP>:8081`
- ✅ Мониторинг и логирование
- ✅ Автоматический перезапуск при сбоях

Приложение готово к использованию в production!