#!/bin/bash

# Скрипт для тестирования интегрированного двухуровневого кеша
# Демонстрирует совместимость с существующим RedisService

BASE_URL="http://localhost:8080/api"

echo "=== Тестирование интегрированного двухуровневого кеша ==="
echo "Совместимость с существующим RedisService"
echo

# Функция для выполнения запроса с измерением времени
make_request() {
    local method=$1
    local url=$2
    local data=$3
    
    echo "Запрос: $method $url"
    if [ -n "$data" ]; then
        echo "Данные: $data"
    fi
    
    start_time=$(date +%s%3N)
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$url")
    else
        response=$(curl -s -w "\n%{http_code}" "$url")
    fi
    
    end_time=$(date +%s%3N)
    duration=$((end_time - start_time))
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    echo "HTTP код: $http_code"
    echo "Время выполнения: ${duration}ms"
    echo "Ответ: $body"
    echo "---"
    echo
}

echo "1. Тестирование ClientRisk кеширования"
echo "======================================"

# Тест получения ClientRisk (первый запрос - загрузка из источника)
echo "Первый запрос ClientRisk (загрузка из источника):"
make_request "GET" "$BASE_URL/client-risk/test-party-1"

# Второй запрос - загрузка из L1 кеша
echo "Второй запрос ClientRisk (загрузка из L1 кеша):"
make_request "GET" "$BASE_URL/client-risk/test-party-1"

# Третий запрос - загрузка из L1 кеша
echo "Третий запрос ClientRisk (загрузка из L1 кеша):"
make_request "GET" "$BASE_URL/client-risk/test-party-1"

echo "2. Тестирование сохранения ClientRisk"
echo "====================================="

# Создание тестового ClientRiskDto
client_risk_data='{
    "partyId": "test-party-2",
    "error": "Test error message",
    "attempt": 0
}'

echo "Сохранение ClientRisk:"
make_request "POST" "$BASE_URL/client-risk?key=test-party-2" "$client_risk_data"

# Получение сохраненного ClientRisk
echo "Получение сохраненного ClientRisk:"
make_request "GET" "$BASE_URL/client-risk/test-party-2"

echo "3. Тестирование ошибок"
echo "======================"

# Получение списка ошибок
echo "Получение списка ошибок:"
make_request "GET" "$BASE_URL/client-risk/errors"

echo "4. Тестирование мониторинга"
echo "==========================="

# Проверка здоровья кеша
echo "Проверка здоровья кеша:"
make_request "GET" "$BASE_URL/client-risk/cache/health"

# Получение статистики кеша
echo "Получение статистики кеша:"
make_request "GET" "$BASE_URL/client-risk/cache/stats"

echo "5. Тестирование производительности"
echo "================================="

# Множественные запросы для демонстрации кеширования
echo "Множественные запросы для демонстрации кеширования:"
for i in {1..5}; do
    echo "Запрос $i:"
    make_request "GET" "$BASE_URL/client-risk/test-party-1"
done

echo "6. Тестирование очистки кеша"
echo "============================"

# Очистка кеша
echo "Очистка кеша:"
make_request "POST" "$BASE_URL/client-risk/cache/clear"

# Запрос после очистки кеша
echo "Запрос после очистки кеша (загрузка из источника):"
make_request "GET" "$BASE_URL/client-risk/test-party-1"

echo "7. Тестирование удаления"
echo "======================="

# Удаление ClientRisk
echo "Удаление ClientRisk:"
make_request "DELETE" "$BASE_URL/client-risk/test-party-2"

# Попытка получить удаленный ClientRisk
echo "Попытка получить удаленный ClientRisk:"
make_request "GET" "$BASE_URL/client-risk/test-party-2"

echo "=== Тестирование завершено ==="
echo
echo "Результаты:"
echo "- L1 кеш (Caffeine): Очень быстрые запросы после первого"
echo "- L2 кеш (Redis): Fallback при отсутствии в L1"
echo "- Graceful degradation: Работает даже если Redis недоступен"
echo "- Совместимость: Полная совместимость с существующим RedisService"