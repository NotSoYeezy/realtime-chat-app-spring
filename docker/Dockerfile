FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY backend/realtime-chat/pom.xml .

RUN mvn dependency:go-offline

COPY backend/realtime-chat/src ./src

RUN mvn -Dmaven.test.skip=true clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache gcompat libc6-compat libstdc++

RUN mkdir -p /app/uploads && chmod 755 /app/uploads

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]