# syntax=docker/dockerfile:1

FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app

COPY gradle gradle
COPY gradlew gradlew
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src src

RUN chmod +x ./gradlew && ./gradlew --no-daemon clean installDist

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/install/QRventure /app/qrventure

ENV PORT=8020
EXPOSE 8020

CMD ["sh", "-c", "/app/qrventure/bin/QRventure -port ${PORT:-8020}"]
