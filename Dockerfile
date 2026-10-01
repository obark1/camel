FROM gradle:jdk21 AS build
WORKDIR /app

COPY gradle /app/gradle
COPY gradlew build.gradle settings.gradle /app/

RUN ./gradlew dependencies --no-daemon

COPY src /app/src
RUN ./gradlew bootJar --no-daemon

# =========================================================================
# Stage 2: Create the lightweight production runtime image
# =========================================================================

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN adduser --system --group spring
USER spring:spring

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]