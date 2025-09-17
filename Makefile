# Makefile для управления двухуровневым кешем

.PHONY: help build run stop clean logs test deploy-ec2 quick-start

# Переменные
DOCKER_COMPOSE_FILE = docker-compose.prod.yml
APP_NAME = two-level-cache
EC2_IP ?= your-ec2-ip
SSH_KEY ?= ~/.ssh/ec2-key.pem

help: ## Показать справку
	@echo "Доступные команды:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Собрать Docker образы
	docker-compose -f $(DOCKER_COMPOSE_FILE) build

run: ## Запустить приложение
	docker-compose -f $(DOCKER_COMPOSE_FILE) up -d

stop: ## Остановить приложение
	docker-compose -f $(DOCKER_COMPOSE_FILE) down

restart: ## Перезапустить приложение
	docker-compose -f $(DOCKER_COMPOSE_FILE) restart

logs: ## Показать логи
	docker-compose -f $(DOCKER_COMPOSE_FILE) logs -f

logs-app: ## Показать логи приложения
	docker-compose -f $(DOCKER_COMPOSE_FILE) logs -f app

logs-redis: ## Показать логи Redis
	docker-compose -f $(DOCKER_COMPOSE_FILE) logs -f redis

status: ## Показать статус контейнеров
	docker-compose -f $(DOCKER_COMPOSE_FILE) ps

clean: ## Очистить контейнеры и образы
	docker-compose -f $(DOCKER_COMPOSE_FILE) down
	docker system prune -f

test: ## Запустить тесты
	./test-routing.sh

test-integration: ## Запустить интеграционные тесты
	./test-integration.sh

deploy-ec2: ## Деплой на EC2
	@if [ "$(EC2_IP)" = "your-ec2-ip" ]; then \
		echo "❌ Ошибка: Укажите EC2_IP"; \
		echo "Использование: make deploy-ec2 EC2_IP=3.15.123.45"; \
		exit 1; \
	fi
	./deploy-ec2.sh $(EC2_IP) $(SSH_KEY)

quick-start: ## Быстрый старт на EC2
	@if [ "$(EC2_IP)" = "your-ec2-ip" ]; then \
		echo "❌ Ошибка: Укажите EC2_IP"; \
		echo "Использование: make quick-start EC2_IP=3.15.123.45"; \
		exit 1; \
	fi
	./quick-start-ec2.sh $(EC2_IP) $(SSH_KEY)

health: ## Проверка здоровья приложения
	@echo "Проверка здоровья приложения..."
	@curl -f http://localhost:8080/actuator/health || echo "Приложение недоступно"

api-info: ## Показать информацию об API
	@curl -s http://localhost:8080/api/routed-cache/info | jq . || curl -s http://localhost:8080/api/routed-cache/info

redis-info: ## Показать информацию о Redis
	@curl -s http://localhost:8080/api/routed-cache/aml-client/cache/health || echo "Redis недоступен"

stats: ## Показать статистику кеша
	@curl -s http://localhost:8080/api/routed-cache/aml-client/cache/stats || echo "Статистика недоступна"

# Команды для разработки
dev: ## Запуск в режиме разработки
	docker-compose up -d

dev-logs: ## Логи в режиме разработки
	docker-compose logs -f

dev-stop: ## Остановка в режиме разработки
	docker-compose down

# Команды для production
prod: ## Запуск в production режиме
	docker-compose -f $(DOCKER_COMPOSE_FILE) up -d

prod-logs: ## Логи в production режиме
	docker-compose -f $(DOCKER_COMPOSE_FILE) logs -f

prod-stop: ## Остановка в production режиме
	docker-compose -f $(DOCKER_COMPOSE_FILE) down

# Команды для мониторинга
monitor: ## Мониторинг ресурсов
	docker stats

redis-cli: ## Подключение к Redis CLI
	docker exec -it two-level-cache-redis redis-cli

# Команды для обновления
update: ## Обновить приложение
	git pull
	docker-compose -f $(DOCKER_COMPOSE_FILE) up --build -d

# Команды для бэкапа
backup-redis: ## Создать бэкап Redis
	docker exec two-level-cache-redis redis-cli BGSAVE
	docker cp two-level-cache-redis:/data/dump.rdb ./backup-$(shell date +%Y%m%d-%H%M%S).rdb

# Команды для очистки
clean-all: ## Полная очистка
	docker-compose -f $(DOCKER_COMPOSE_FILE) down
	docker system prune -a -f
	docker volume prune -f

# Команды для тестирования производительности
perf-test: ## Тест производительности
	@echo "Запуск тестов производительности..."
	@for i in {1..10}; do \
		echo "Запрос $$i:"; \
		time curl -s http://localhost:8080/api/routed-cache/aml-client/test-$$i > /dev/null; \
	done

# Команды для отладки
debug: ## Отладка приложения
	docker-compose -f $(DOCKER_COMPOSE_FILE) exec app sh

debug-redis: ## Отладка Redis
	docker-compose -f $(DOCKER_COMPOSE_FILE) exec redis sh