#!/bin/bash

# Скрипт для тестирования маршрутизации кешей
# Демонстрирует как разные типы данных маршрутизируются в разные кеши

BASE_URL="http://localhost:8080/api/routed-cache"

echo "=== Тестирование маршрутизации кешей ==="
echo "Демонстрация гибкой маршрутизации данных в разные кеши"
echo

# Функция для выполнения запроса с измерением времени
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local description=$4
    
    echo "=== $description ==="
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
    echo
}

echo "1. Тестирование AmlClient (ТОЛЬКО Redis - L2)"
echo "=============================================="

# Создание тестового AmlClient
aml_client_data='{
    "partyId": "test-party-123",
    "error": "Test error message",
    "attempt": 0
}'

make_request "POST" "$BASE_URL/aml-client/test-party-123" "$aml_client_data" "Сохранение AmlClient (Redis only)"

# Получение AmlClient (первый запрос - загрузка из источника)
make_request "GET" "$BASE_URL/aml-client/test-party-123" "" "Первый запрос AmlClient (загрузка из источника)"

# Второй запрос - загрузка из Redis
make_request "GET" "$BASE_URL/aml-client/test-party-123" "" "Второй запрос AmlClient (загрузка из Redis)"

# Третий запрос - загрузка из Redis
make_request "GET" "$BASE_URL/aml-client/test-party-123" "" "Третий запрос AmlClient (загрузка из Redis)"

echo "2. Тестирование пользовательских данных (ДВУХУРОВНЕВЫЙ кеш - L1 + L2)"
echo "====================================================================="

# Первый запрос пользовательских данных - загрузка из источника
make_request "GET" "$BASE_URL/user-data/user-456" "" "Первый запрос пользовательских данных (загрузка из источника)"

# Второй запрос - загрузка из L1 кеша (Caffeine)
make_request "GET" "$BASE_URL/user-data/user-456" "" "Второй запрос пользовательских данных (загрузка из L1 кеша)"

# Третий запрос - загрузка из L1 кеша (Caffeine)
make_request "GET" "$BASE_URL/user-data/user-456" "" "Третий запрос пользовательских данных (загрузка из L1 кеша)"

# Сохранение пользовательских данных
user_data='"Updated user data for user-456"'
make_request "POST" "$BASE_URL/user-data/user-456" "$user_data" "Сохранение пользовательских данных (L1 + L2)"

echo "3. Тестирование временных данных (ТОЛЬКО Caffeine - L1)"
echo "======================================================"

# Первый запрос временных данных - загрузка из источника
make_request "GET" "$BASE_URL/temp-data/temp-789" "" "Первый запрос временных данных (загрузка из источника)"

# Второй запрос - загрузка из L1 кеша (Caffeine)
make_request "GET" "$BASE_URL/temp-data/temp-789" "" "Второй запрос временных данных (загрузка из L1 кеша)"

# Третий запрос - загрузка из L1 кеша (Caffeine)
make_request "GET" "$BASE_URL/temp-data/temp-789" "" "Третий запрос временных данных (загрузка из L1 кеша)"

# Сохранение временных данных
temp_data='"Updated temp data for temp-789"'
make_request "POST" "$BASE_URL/temp-data/temp-789" "$temp_data" "Сохранение временных данных (только L1 кеш)"

echo "4. Тестирование конфигурационных данных (ТОЛЬКО Caffeine - L1)"
echo "============================================================="

# Первый запрос конфигурационных данных - загрузка из источника
make_request "GET" "$BASE_URL/config-data/database-url" "" "Первый запрос конфигурационных данных (загрузка из источника)"

# Второй запрос - загрузка из L1 кеша (Caffeine)
make_request "GET" "$BASE_URL/config-data/database-url" "" "Второй запрос конфигурационных данных (загрузка из L1 кеша)"

echo "5. Тестирование статистики (ДВУХУРОВНЕВЫЙ кеш - L1 + L2)"
echo "======================================================="

# Первый запрос статистики - загрузка из источника
make_request "GET" "$BASE_URL/statistics/daily-stats" "" "Первый запрос статистики (загрузка из источника)"

# Второй запрос - загрузка из L1 кеша (Caffeine)
make_request "GET" "$BASE_URL/statistics/daily-stats" "" "Второй запрос статистики (загрузка из L1 кеша)"

# Третий запрос - загрузка из L1 кеша (Caffeine)
make_request "GET" "$BASE_URL/statistics/daily-stats" "" "Третий запрос статистики (загрузка из L1 кеша)"

echo "6. Тестирование ошибок AmlClient (ТОЛЬКО Redis - L2)"
echo "===================================================="

# Получение ошибок AmlClient
make_request "GET" "$BASE_URL/aml-client/errors" "" "Получение ошибок AmlClient (Redis only)"

# Обработка ошибок AmlClient
make_request "POST" "$BASE_URL/aml-client/process-errors" "" "Обработка ошибок AmlClient"

echo "7. Тестирование производительности"
echo "================================="

echo "Множественные запросы для демонстрации кеширования:"

# AmlClient (Redis only)
echo "AmlClient (Redis only):"
for i in {1..3}; do
    make_request "GET" "$BASE_URL/aml-client/test-party-123" "" "AmlClient запрос $i"
done

# Пользовательские данные (Two-level)
echo "Пользовательские данные (Two-level):"
for i in {1..3}; do
    make_request "GET" "$BASE_URL/user-data/user-456" "" "User data запрос $i"
done

# Временные данные (Caffeine only)
echo "Временные данные (Caffeine only):"
for i in {1..3}; do
    make_request "GET" "$BASE_URL/temp-data/temp-789" "" "Temp data запрос $i"
done

echo "8. Информация о маршрутизации"
echo "============================"

make_request "GET" "$BASE_URL/info" "" "Информация о маршрутизации кешей"

echo "9. Очистка кешей"
echo "==============="

make_request "POST" "$BASE_URL/clear" "" "Очистка кешей"

echo "=== Тестирование завершено ==="
echo
echo "Результаты маршрутизации:"
echo "- AmlClient: ТОЛЬКО Redis (L2) - для обмена между экземплярами"
echo "- Пользовательские данные: L1 + L2 - для скорости и доступности"
echo "- Временные данные: ТОЛЬКО Caffeine (L1) - для быстрого доступа"
echo "- Конфигурационные данные: ТОЛЬКО Caffeine (L1) - редко изменяются"
echo "- Статистика: L1 + L2 - для скорости и обмена данными"
echo
echo "Преимущества маршрутизации:"
echo "- Гибкость: каждый тип данных в оптимальном кеше"
echo "- Производительность: максимальная скорость для каждого случая"
echo "- Экономия ресурсов: не кешируем ненужные данные"
echo "- Масштабируемость: правильное распределение нагрузки"