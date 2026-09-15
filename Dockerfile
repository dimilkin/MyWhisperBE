# Stage 1: build the application with the project's own Maven wrapper
FROM eclipse-temurin:21-jdk-alpine AS build

RUN apk add --no-cache curl

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: run the built jar
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", \
    "-Xms128m", \
    "-Xmx400m", \
    "-XX:+UseG1GC", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:+UseContainerSupport", \
    "-jar", "app.jar"]
