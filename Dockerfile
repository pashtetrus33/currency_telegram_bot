# Используем официальный образ JDK
FROM openjdk:17-alpine AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем необходимые файлы
COPY gradlew ./
COPY gradle/ ./gradle/
COPY build.gradle ./
COPY settings.gradle ./
COPY src/ ./src/

# Даем права на выполнение для gradlew
RUN chmod +x gradlew

# Собираем приложение с помощью Gradle
RUN ./gradlew build --no-daemon

# Используем официальный образ JRE для запуска приложения
FROM openjdk:17-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранное приложение из предыдущего образа
COPY --from=build /app/build/libs/*.jar app.jar

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
