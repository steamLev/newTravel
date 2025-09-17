#!/bin/bash

# Скрипт для деплоя двухуровневого кеша на Amazon EC2
# Использование: ./deploy-ec2.sh [EC2_IP] [SSH_KEY]

set -e

# Параметры
EC2_IP=${1:-"your-ec2-ip"}
SSH_KEY=${2:-"~/.ssh/your-key.pem"}
APP_NAME="two-level-cache"
DOCKER_COMPOSE_FILE="docker-compose.prod.yml"

echo "=== Деплой двухуровневого кеша на EC2 ==="
echo "EC2 IP: $EC2_IP"
echo "SSH Key: $SSH_KEY"
echo

# Проверяем параметры
if [ "$EC2_IP" = "your-ec2-ip" ]; then
    echo "❌ Ошибка: Укажите IP адрес EC2 instance"
    echo "Использование: ./deploy-ec2.sh <EC2_IP> [SSH_KEY]"
    exit 1
fi

if [ ! -f "$SSH_KEY" ]; then
    echo "❌ Ошибка: SSH ключ не найден: $SSH_KEY"
    exit 1
fi

echo "1. Подготовка файлов для деплоя..."

# Создаем архив с проектом
echo "Создание архива проекта..."
tar -czf ${APP_NAME}.tar.gz \
    --exclude='.git' \
    --exclude='target' \
    --exclude='*.log' \
    --exclude='.idea' \
    --exclude='node_modules' \
    .

echo "✅ Архив создан: ${APP_NAME}.tar.gz"

echo "2. Копирование файлов на EC2..."

# Копируем архив на EC2
scp -i "$SSH_KEY" ${APP_NAME}.tar.gz ec2-user@$EC2_IP:~/

echo "✅ Файлы скопированы на EC2"

echo "3. Подключение к EC2 и настройка..."

# Подключаемся к EC2 и выполняем настройку
ssh -i "$SSH_KEY" ec2-user@$EC2_IP << 'EOF'
    echo "=== Настройка EC2 instance ==="
    
    # Обновляем систему
    echo "Обновление системы..."
    sudo yum update -y
    
    # Устанавливаем Docker
    echo "Установка Docker..."
    sudo yum install -y docker
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -a -G docker ec2-user
    
    # Устанавливаем Docker Compose
    echo "Установка Docker Compose..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    
    # Устанавливаем дополнительные утилиты
    echo "Установка дополнительных утилит..."
    sudo yum install -y curl wget git htop
    
    # Создаем директорию для приложения
    echo "Создание директории приложения..."
    mkdir -p ~/apps/$APP_NAME
    cd ~/apps/$APP_NAME
    
    # Распаковываем архив
    echo "Распаковка архива..."
    tar -xzf ~/${APP_NAME}.tar.gz
    
    # Устанавливаем права доступа
    chmod +x *.sh
    
    echo "✅ Настройка EC2 завершена"
EOF

echo "4. Запуск приложения на EC2..."

# Запускаем приложение
ssh -i "$SSH_KEY" ec2-user@$EC2_IP << EOF
    cd ~/apps/$APP_NAME
    
    echo "=== Запуск приложения ==="
    
    # Останавливаем существующие контейнеры
    echo "Остановка существующих контейнеров..."
    docker-compose -f $DOCKER_COMPOSE_FILE down || true
    
    # Собираем и запускаем приложение
    echo "Сборка и запуск приложения..."
    docker-compose -f $DOCKER_COMPOSE_FILE up --build -d
    
    # Ждем запуска
    echo "Ожидание запуска сервисов..."
    sleep 30
    
    # Проверяем статус
    echo "Проверка статуса сервисов..."
    docker-compose -f $DOCKER_COMPOSE_FILE ps
    
    # Проверяем здоровье приложения
    echo "Проверка здоровья приложения..."
    curl -f http://localhost:8080/actuator/health || echo "Приложение еще запускается..."
    
    echo "✅ Приложение запущено!"
EOF

echo "5. Проверка деплоя..."

# Проверяем доступность приложения
echo "Проверка доступности приложения..."
sleep 10

if curl -f http://$EC2_IP:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Приложение доступно по адресу: http://$EC2_IP:8080"
    echo "✅ API документация: http://$EC2_IP:8080/api/routed-cache/info"
    echo "✅ Redis Commander: http://$EC2_IP:8081"
else
    echo "⚠️  Приложение еще запускается. Попробуйте через несколько минут."
fi

echo
echo "=== Деплой завершен ==="
echo "Полезные команды для управления:"
echo "  SSH: ssh -i $SSH_KEY ec2-user@$EC2_IP"
echo "  Логи: docker-compose -f $DOCKER_COMPOSE_FILE logs -f"
echo "  Остановка: docker-compose -f $DOCKER_COMPOSE_FILE down"
echo "  Перезапуск: docker-compose -f $DOCKER_COMPOSE_FILE restart"
echo
echo "Очистка локальных файлов..."
rm -f ${APP_NAME}.tar.gz

echo "✅ Готово!"