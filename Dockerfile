# Stage 1: Build with JDK
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:resolve -q
COPY src src
RUN ./mvnw clean package -DskipTests -q

# Stage 2: Run with JRE (much smaller memory!)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx180m", "-Xms40m", "-XX:+UseSerialGC", "-XX:MaxMetaspaceSize=80m", "-XX:TieredStopAtLevel=1", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]