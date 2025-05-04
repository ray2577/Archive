FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
# 预先下载依赖项
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/uploads

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="docker"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 