# Multi-stage build для оптимизации размера образа
FROM maven:3.9.4-openjdk-17-slim AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем pom.xml и загружаем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jre-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Создаем пользователя для безопасности
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Копируем JAR файл из build stage
COPY --from=build /app/target/*.jar app.jar

# Устанавливаем права доступа
RUN chown -R appuser:appuser /app
USER appuser

# Открываем порт
EXPOSE 8080

# Настройки JVM для production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Команда запуска
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]