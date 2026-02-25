FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY target/scraper-scheduler-1.0.0-SNAPSHOT.jar app.jar

RUN addgroup -S scraper && adduser -S scraper -G scraper
USER scraper

EXPOSE 8082
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]