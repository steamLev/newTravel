#!/bin/bash

echo "🚀 Запуск Spring приложения с Consul и мониторингом..."

# Проверяем, есть ли Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не найден. Установите Docker для запуска"
    exit 1
fi

# Останавливаем существующие контейнеры
echo "🛑 Останавливаем существующие контейнеры..."
docker-compose -f docker-compose-consul.yml down

# Собираем приложение
echo "🔨 Собираем Spring Boot приложение..."
docker build -t spring-app-consul .

# Запускаем инфраструктуру
echo "📦 Запускаем Consul, RabbitMQ, Nginx, Prometheus и Grafana..."
docker-compose -f docker-compose-consul.yml up -d

# Ждем запуска сервисов
echo "⏳ Ждем запуска сервисов..."
sleep 60

# Проверяем статус
echo "🔍 Проверяем статус сервисов..."
docker-compose -f docker-compose-consul.yml ps

echo ""
echo "✅ Сервисы запущены:"
echo "🔌 Consul: http://localhost:8500"
echo "🐰 RabbitMQ: http://localhost:15672 (guest/guest)"
echo "🌐 Spring App: http://localhost:8080"
echo "⚖️  Nginx LB: http://localhost:80"
echo "📊 Prometheus: http://localhost:9090"
echo "📈 Grafana: http://localhost:3000 (admin/admin)"
echo ""
echo "🔍 Проверка здоровья:"
echo "   curl http://localhost:8080/actuator/health"
echo "   curl http://localhost/health"
echo ""
echo "📋 Consul UI:"
echo "   http://localhost:8500/ui"
echo ""
echo "🧪 Тестирование:"
echo "   curl -X POST http://localhost:8080/api/messages/send/single -H 'Content-Type: application/json' -d '{\"id\":\"test\",\"content\":\"Hello\",\"type\":\"test\"}'"
echo ""
echo "🛑 Graceful shutdown:"
echo "   curl -X POST http://localhost:8080/api/lifecycle/shutdown"