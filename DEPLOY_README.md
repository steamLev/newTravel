# Деплой двухуровневого кеша на Amazon EC2

## 🚀 Быстрый старт

### 1. Автоматический деплой (рекомендуется)

```bash
# Клонируйте репозиторий
git clone <your-repo-url>
cd two-level-cache

# Запустите быстрый деплой
./quick-start-ec2.sh <EC2_IP> <SSH_KEY>

# Пример:
./quick-start-ec2.sh 3.15.123.45 ~/.ssh/my-key.pem
```

### 2. Полный деплой с настройкой

```bash
# Запустите полный деплой
./deploy-ec2.sh <EC2_IP> <SSH_KEY>

# Пример:
./deploy-ec2.sh 3.15.123.45 ~/.ssh/my-key.pem
```

### 3. Использование Makefile

```bash
# Быстрый старт
make quick-start EC2_IP=3.15.123.45

# Полный деплой
make deploy-ec2 EC2_IP=3.15.123.45

# Проверка статуса
make status

# Просмотр логов
make logs
```

## 📋 Предварительные требования

### Amazon EC2 Instance
- **OS**: Amazon Linux 2 или Ubuntu 20.04+
- **Instance Type**: t3.medium или больше
- **Storage**: минимум 20 GB
- **Security Groups**: порты 22, 80, 8080, 8081

### Локальная машина
- SSH клиент
- Git (опционально)

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

# Перелогиниться для применения группы docker
exit
ssh -i ~/.ssh/your-key.pem ec2-user@<EC2_IP>
```

### 3. Установка Docker Compose

```bash
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 4. Копирование проекта

```bash
# Создаем директорию
mkdir -p ~/two-level-cache
cd ~/two-level-cache

# Копируем файлы с локальной машины
scp -i ~/.ssh/your-key.pem -r . ec2-user@<EC2_IP>:~/two-level-cache/
```

### 5. Запуск приложения

```bash
# Запускаем в production режиме
docker-compose -f docker-compose.prod.yml up -d

# Проверяем статус
docker-compose -f docker-compose.prod.yml ps
```

## 🌐 Доступ к приложению

После успешного деплоя приложение будет доступно по адресам:

- **Основное приложение**: http://<EC2_IP>:8080
- **API документация**: http://<EC2_IP>:8080/api/routed-cache/info
- **Health check**: http://<EC2_IP>:8080/actuator/health
- **Redis Commander**: http://<EC2_IP>:8081

## 🧪 Тестирование

### 1. Проверка здоровья

```bash
curl http://<EC2_IP>:8080/actuator/health
```

### 2. Тестирование API

```bash
# AmlClient (Redis only)
curl http://<EC2_IP>:8080/api/routed-cache/aml-client/test-123

# User data (Two-level cache)
curl http://<EC2_IP>:8080/api/routed-cache/user-data/user-456

# Temp data (Caffeine only)
curl http://<EC2_IP>:8080/api/routed-cache/temp-data/temp-789
```

### 3. Запуск тестов

```bash
# На EC2 instance
cd ~/two-level-cache
./test-routing.sh
```

## 📊 Мониторинг

### 1. Статус контейнеров

```bash
docker-compose -f docker-compose.prod.yml ps
```

### 2. Логи

```bash
# Все логи
docker-compose -f docker-compose.prod.yml logs -f

# Логи приложения
docker-compose -f docker-compose.prod.yml logs -f app

# Логи Redis
docker-compose -f docker-compose.prod.yml logs -f redis
```

### 3. Использование ресурсов

```bash
docker stats
```

### 4. Redis мониторинг

Откройте в браузере: http://<EC2_IP>:8081

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
# Остановить
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
```

## 🚨 Troubleshooting

### 1. Приложение не запускается

```bash
# Проверьте логи
docker-compose -f docker-compose.prod.yml logs app

# Проверьте порты
netstat -tlnp | grep :8080
```

### 2. Redis недоступен

```bash
# Проверьте статус Redis
docker-compose -f docker-compose.prod.yml ps redis

# Перезапустите Redis
docker-compose -f docker-compose.prod.yml restart redis
```

### 3. Высокое потребление памяти

```bash
# Проверьте использование памяти
docker stats

# Ограничьте память в docker-compose.prod.yml
```

## 📈 Оптимизация

### 1. Настройка JVM

Добавьте в `docker-compose.prod.yml`:

```yaml
environment:
  - JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC
```

### 2. Настройка Redis

```yaml
command: redis-server --appendonly yes --maxmemory 512mb --maxmemory-policy allkeys-lru
```

### 3. Настройка Nginx

```yaml
environment:
  - NGINX_WORKER_PROCESSES=auto
  - NGINX_WORKER_CONNECTIONS=1024
```

## 🔒 Безопасность

### 1. Security Groups

Убедитесь что открыты только необходимые порты:

| Port | Protocol | Source | Description |
|------|----------|--------|-------------|
| 22 | TCP | Your IP | SSH |
| 80 | TCP | 0.0.0.0/0 | HTTP |
| 8080 | TCP | 0.0.0.0/0 | Application |
| 8081 | TCP | Your IP | Redis Commander |

### 2. Firewall

```bash
# Настройте iptables
sudo iptables -A INPUT -p tcp --dport 22 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 80 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
sudo iptables -A INPUT -j DROP
```

### 3. SSL/TLS

```bash
# Используйте Let's Encrypt
sudo yum install -y certbot
sudo certbot certonly --standalone -d your-domain.com
```

## 📚 Полезные команды

### Docker

```bash
# Просмотр контейнеров
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

## 📞 Поддержка

Если у вас возникли проблемы:

1. Проверьте логи: `docker-compose -f docker-compose.prod.yml logs -f`
2. Проверьте статус: `docker-compose -f docker-compose.prod.yml ps`
3. Проверьте ресурсы: `docker stats`
4. Обратитесь к документации: `EC2_SETUP_GUIDE.md`