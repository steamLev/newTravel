# 1. Используем базовый образ с JDK
FROM openjdk:21-jdk-slim

# 2. Указываем рабочую директорию
WORKDIR /app

# 3. Копируем файл jar в контейнер
COPY target/TravelCentralAsia.jar /app/app.jar

# 4. Устанавливаем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
