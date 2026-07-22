FROM eclipse-temurin:21-jdk AS build
RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*
WORKDIR /src
COPY pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY . .
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:21-jre
COPY --from=build /src/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
