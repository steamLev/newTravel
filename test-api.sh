#!/bin/bash

# Скрипт для тестирования API RabbitMQ Message Sender

BASE_URL="http://localhost:8080/api/messages"

echo "=== Тестирование RabbitMQ Message Sender API ==="
echo

# Проверка статуса каналов
echo "1. Проверка статуса каналов:"
curl -s "$BASE_URL/channels/status" | jq '.' 2>/dev/null || echo "Ошибка получения статуса каналов"
echo

# Проверка доступности каналов
echo "2. Проверка доступности каналов:"
curl -s "$BASE_URL/channels/available" | jq '.' 2>/dev/null || echo "Ошибка проверки доступности каналов"
echo

# Создание тестовых сообщений
echo "3. Создание тестовых сообщений:"
curl -s "$BASE_URL/test/create?count=3&type=demo" | jq '.' 2>/dev/null || echo "Ошибка создания тестовых сообщений"
echo

# Отправка тестовых сообщений
echo "4. Отправка тестовых сообщений:"
curl -s -X POST "$BASE_URL/test/send?count=3&type=demo&routingKey=demo.key" | jq '.' 2>/dev/null || echo "Ошибка отправки тестовых сообщений"
echo

# Отправка одного сообщения
echo "5. Отправка одного сообщения:"
curl -s -X POST "$BASE_URL/send/single" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Демонстрационное сообщение",
    "type": "demo",
    "priority": "high"
  }' | jq '.' 2>/dev/null || echo "Ошибка отправки одного сообщения"
echo

# Отправка списка сообщений
echo "6. Отправка списка сообщений:"
curl -s -X POST "$BASE_URL/send" \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {
        "content": "Сообщение 1",
        "type": "demo",
        "priority": "high"
      },
      {
        "content": "Сообщение 2", 
        "type": "demo",
        "priority": "medium"
      },
      {
        "content": "Сообщение 3",
        "type": "demo", 
        "priority": "low"
      }
    ],
    "routingKey": "demo.batch.key"
  }' | jq '.' 2>/dev/null || echo "Ошибка отправки списка сообщений"
echo

echo "=== Тестирование завершено ==="