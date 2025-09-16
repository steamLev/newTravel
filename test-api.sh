#!/bin/bash

# Скрипт для тестирования API двухуровневого кеша
# Убедитесь, что приложение запущено на localhost:8080

BASE_URL="http://localhost:8080/api"

echo "=== Тестирование двухуровневого кеша ==="
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

echo "1. Тестирование пользователей"
echo "=============================="

# Первый запрос - загрузка из БД
echo "Первый запрос пользователя (загрузка из БД):"
make_request "GET" "$BASE_URL/users/1"

# Второй запрос - загрузка из кеша
echo "Второй запрос пользователя (загрузка из кеша):"
make_request "GET" "$BASE_URL/users/1"

# Третий запрос - загрузка из кеша
echo "Третий запрос пользователя (загрузка из кеша):"
make_request "GET" "$BASE_URL/users/1"

echo "2. Тестирование продуктов"
echo "=========================="

# Поиск продуктов по категории
echo "Поиск продуктов по категории 'Электроника':"
make_request "GET" "$BASE_URL/products/category/Электроника"

# Повторный поиск (из кеша)
echo "Повторный поиск продуктов по категории (из кеша):"
make_request "GET" "$BASE_URL/products/category/Электроника"

# Поиск по ценовому диапазону
echo "Поиск продуктов в ценовом диапазоне 1000-20000:"
make_request "GET" "$BASE_URL/products/price-range?min=1000&max=20000"

# Поиск по названию
echo "Поиск продуктов по названию 'ноутбук':"
make_request "GET" "$BASE_URL/products/search?q=ноутбук"

echo "3. Тестирование создания и обновления"
echo "======================================"

# Создание нового пользователя
echo "Создание нового пользователя:"
user_data='{"firstName":"Тест","lastName":"Пользователь","email":"test@example.com"}'
make_request "POST" "$BASE_URL/users" "$user_data"

# Создание нового продукта
echo "Создание нового продукта:"
product_data='{"name":"Тестовый продукт","category":"Тест","price":1000,"stock":1}'
make_request "POST" "$BASE_URL/products" "$product_data"

echo "4. Тестирование очистки кеша"
echo "============================="

# Очистка кеша пользователей
echo "Очистка кеша пользователей:"
make_request "POST" "$BASE_URL/users/cache/clear"

# Очистка кеша продуктов
echo "Очистка кеша продуктов:"
make_request "POST" "$BASE_URL/products/cache/clear"

echo "5. Тестирование после очистки кеша"
echo "==================================="

# Запрос после очистки кеша (загрузка из БД)
echo "Запрос пользователя после очистки кеша (загрузка из БД):"
make_request "GET" "$BASE_URL/users/1"

echo "=== Тестирование завершено ==="