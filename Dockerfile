FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

COPY target/sprout-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080
EXPOSE 8080

CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
