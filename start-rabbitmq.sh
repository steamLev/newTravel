#!/bin/bash

echo "🚀 Запуск RabbitMQ..."

# Проверяем, есть ли Docker
if command -v docker &> /dev/null; then
    echo "📦 Используем Docker для запуска RabbitMQ..."
    docker run -d \
        --name rabbitmq-test \
        -p 5672:5672 \
        -p 15672:15672 \
        -e RABBITMQ_DEFAULT_USER=guest \
        -e RABBITMQ_DEFAULT_PASS=guest \
        rabbitmq:3-management
    
    echo "✅ RabbitMQ запущен в Docker контейнере"
    echo "🌐 Management UI: http://localhost:15672 (guest/guest)"
    echo "🔌 AMQP порт: localhost:5672"
    
else
    echo "❌ Docker не найден. Установите RabbitMQ вручную:"
    echo "   sudo apt update && sudo apt install -y rabbitmq-server"
    echo "   sudo systemctl start rabbitmq-server"
    echo "   sudo systemctl enable rabbitmq-server"
fi