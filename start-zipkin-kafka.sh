#!/bin/bash

echo "🚀 Запуск Spring Boot + Kafka + Zipkin для распределенной трассировки..."

# Проверяем, есть ли Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не найден. Установите Docker для запуска"
    exit 1
fi

# Останавливаем существующие контейнеры
echo "🛑 Останавливаем существующие контейнеры..."
docker-compose -f docker-compose-zipkin.yml down

# Собираем приложение
echo "🔨 Собираем Spring Boot приложение с Zipkin трассировкой..."
docker build -t spring-app-zipkin .

# Запускаем инфраструктуру
echo "📦 Запускаем Kafka, Zipkin, Prometheus и Grafana..."
docker-compose -f docker-compose-zipkin.yml up -d

# Ждем запуска сервисов
echo "⏳ Ждем запуска сервисов..."
sleep 60

# Проверяем статус
echo "🔍 Проверяем статус сервисов..."
docker-compose -f docker-compose-zipkin.yml ps

echo ""
echo "✅ Сервисы запущены:"
echo "🔌 Kafka: localhost:9092"
echo "📊 Zipkin: http://localhost:9411"
echo "🌐 Spring App: http://localhost:8080"
echo "📈 Kafka UI: http://localhost:8081"
echo "📊 Prometheus: http://localhost:9090"
echo "📈 Grafana: http://localhost:3000 (admin/admin)"
echo ""
echo "🧪 Тестирование Zipkin трассировки:"
echo "   curl -X POST http://localhost:8080/api/zipkin/test"
echo "   curl -X POST 'http://localhost:8080/api/zipkin/user-event?userId=123&action=login'"
echo "   curl -X POST 'http://localhost:8080/api/zipkin/send?topic=test-topic&message=Hello Zipkin'"
echo "   curl -X POST http://localhost:8080/api/zipkin/chain-test"
echo ""
echo "🔍 Zipkin UI для просмотра трассировок:"
echo "   http://localhost:9411"
echo ""
echo "📊 Kafka UI для просмотра сообщений:"
echo "   http://localhost:8081"
echo ""
echo "📈 Grafana для мониторинга:"
echo "   http://localhost:3000 (admin/admin)"
echo ""
echo "🔍 Проверка здоровья:"
echo "   curl http://localhost:8080/actuator/health"
echo "   curl http://localhost:9411/health"