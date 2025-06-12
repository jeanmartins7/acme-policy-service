
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app


ENV JAVA_HOME="/opt/java/openjdk"
ENV PATH="${JAVA_HOME}/bin:${PATH}"


COPY pom.xml .


COPY src ./src


COPY mvnw .
COPY .mvn .mvn


RUN ./mvnw clean package -Pnative -DskipTests -Dspring-boot.build-image.skip=true


FROM alpine/glibc


WORKDIR /app


COPY --from=builder /app/target/*.jar ./app.jar
COPY --from=builder /app/target/order-microservice-0.0.1-SNAPSHOT.jar ./order-microservice-0.0.1-SNAPSHOT.jar

COPY src/main/resources/application.yml ./config/application.yml
COPY src/main/resources/application-local.yml ./config/application-local.yml

EXPOSE 8080

ENV OTEL_SERVICE_NAME="order-microservice"
ENV OTEL_TRACES_EXPORTER="otlp"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://collector:4318"
ENV OTEL_EXPORTER_OTLP_TRACES_PROTOCOL="http/protobuf"

ENTRYPOINT ["./order-microservice"]
