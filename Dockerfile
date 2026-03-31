FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:resolve -q
COPY src src
RUN ./mvnw clean package -DskipTests -q
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx200m", "-Xms50m", "-XX:+UseSerialGC", "-jar", "target/TempleArchanaSystem-0.0.1-SNAPSHOT.jar"]