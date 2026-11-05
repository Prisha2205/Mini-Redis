# Stage 1: Build the JAR with Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Lightweight runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Folder where appendonly.aof will be saved
RUN mkdir -p /data

# Copy the built jar from stage 1
COPY --from=builder /app/target/mini-redis-1.0.0.jar app.jar

ENV PORT=6380 \
    AOF_PATH=/data/appendonly.aof \
    MAX_KEYS=1000

EXPOSE 6380

ENTRYPOINT ["java", "-jar", "app.jar"]