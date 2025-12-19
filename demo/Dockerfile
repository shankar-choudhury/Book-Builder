# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only pom first so dependency downloads are cached
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Now copy sources and build
COPY src ./src
RUN mvn -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar (avoid accidentally grabbing *-plain.jar if it exists)
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

# Optional: JVM flags are nice in containers; keep simple for now
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
