#!/bin/bash

echo "🚀 Запуск Kafka с мониторингом..."

# Проверяем, есть ли Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не найден. Установите Docker для запуска Kafka"
    exit 1
fi

# Останавливаем существующие контейнеры
echo "🛑 Останавливаем существующие контейнеры..."
docker-compose -f docker-compose-kafka.yml down

# Запускаем Kafka инфраструктуру
echo "📦 Запускаем Kafka, Zookeeper, Prometheus и Grafana..."
docker-compose -f docker-compose-kafka.yml up -d

# Ждем запуска сервисов
echo "⏳ Ждем запуска сервисов..."
sleep 30

# Проверяем статус
echo "🔍 Проверяем статус сервисов..."
docker-compose -f docker-compose-kafka.yml ps

echo ""
echo "✅ Сервисы запущены:"
echo "🔌 Kafka: localhost:9092"
echo "🌐 Kafka UI: http://localhost:8080"
echo "📊 Prometheus: http://localhost:9090"
echo "📈 Grafana: http://localhost:3000 (admin/admin)"
echo ""
echo "📝 Для проверки топиков:"
echo "   docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list"
echo ""
echo "📤 Для отправки тестового сообщения:"
echo "   curl -X POST http://localhost:8080/api/kafka/test"
echo ""
echo "🔍 Для проверки метрик в Prometheus:"
echo "   http://localhost:9090/targets"