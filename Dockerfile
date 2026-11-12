# Etapa 1: compilar el jar (los tests se omiten: BackendApplicationTests necesita una BD viva)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

# Etapa 2: imagen liviana solo con el JRE
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home spring
COPY --from=build /app/target/*.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
