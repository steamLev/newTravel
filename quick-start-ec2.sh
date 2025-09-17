#!/bin/bash

# Быстрый старт на EC2
# Использование: ./quick-start-ec2.sh <EC2_IP> [SSH_KEY]

set -e

EC2_IP=${1:-"your-ec2-ip"}
SSH_KEY=${2:-"~/.ssh/ec2-key.pem"}

echo "=== Быстрый старт двухуровневого кеша на EC2 ==="
echo "EC2 IP: $EC2_IP"
echo

if [ "$EC2_IP" = "your-ec2-ip" ]; then
    echo "❌ Ошибка: Укажите IP адрес EC2 instance"
    echo "Использование: ./quick-start-ec2.sh <EC2_IP> [SSH_KEY]"
    exit 1
fi

echo "1. Подключение к EC2 и установка Docker..."

ssh -i "$SSH_KEY" ec2-user@$EC2_IP << 'EOF'
    # Обновляем систему
    sudo yum update -y
    
    # Устанавливаем Docker
    sudo yum install -y docker
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -a -G docker ec2-user
    
    # Устанавливаем Docker Compose
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    
    # Устанавливаем curl
    sudo yum install -y curl
    
    echo "✅ Docker установлен"
EOF

echo "2. Копирование файлов на EC2..."

# Создаем временный архив
tar -czf temp-app.tar.gz \
    --exclude='.git' \
    --exclude='target' \
    --exclude='*.log' \
    --exclude='.idea' \
    --exclude='node_modules' \
    .

# Копируем на EC2
scp -i "$SSH_KEY" temp-app.tar.gz ec2-user@$EC2_IP:~/

echo "3. Запуск приложения на EC2..."

ssh -i "$SSH_KEY" ec2-user@$EC2_IP << 'EOF'
    # Создаем директорию
    mkdir -p ~/two-level-cache
    cd ~/two-level-cache
    
    # Распаковываем архив
    tar -xzf ~/temp-app.tar.gz
    
    # Запускаем приложение
    echo "Запуск приложения..."
    docker-compose -f docker-compose.prod.yml up -d
    
    # Ждем запуска
    echo "Ожидание запуска..."
    sleep 30
    
    # Проверяем статус
    docker-compose -f docker-compose.prod.yml ps
    
    echo "✅ Приложение запущено!"
EOF

echo "4. Проверка приложения..."

sleep 10

if curl -f http://$EC2_IP:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Приложение доступно!"
    echo "🌐 URL: http://$EC2_IP:8080"
    echo "📊 API Info: http://$EC2_IP:8080/api/routed-cache/info"
    echo "🔧 Redis Commander: http://$EC2_IP:8081"
    echo
    echo "Тестовые команды:"
    echo "curl http://$EC2_IP:8080/api/routed-cache/aml-client/test-123"
    echo "curl http://$EC2_IP:8080/api/routed-cache/user-data/user-456"
else
    echo "⚠️  Приложение еще запускается..."
    echo "Проверьте через несколько минут: http://$EC2_IP:8080"
fi

# Очистка
rm -f temp-app.tar.gz

echo
echo "=== Готово! ==="