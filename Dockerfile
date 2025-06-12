
FROM eclipse-temurin:17-jdk-jammy AS build

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Define explicitamente JAVA_HOME e adiciona o binário Java ao PATH.
# Com base no seu log de depuração, 'which java' aponta para /opt/java/openjdk/bin/java.
# Portanto, JAVA_HOME deve ser /opt/java/openjdk.
ENV JAVA_HOME="/opt/java/openjdk"
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Copia o arquivo pom.xml para que as dependências possam ser baixadas primeiro
COPY pom.xml .

# Copia o código fonte da aplicação
COPY src ./src

# Copia o Maven Wrapper e sua pasta de configuração
COPY mvnw .
COPY .mvn .mvn

# Compila a aplicação para gerar o EXECUTÁVEL NATIVO.
# Os comandos de diagnóstico anteriores confirmaram o problema, agora devem ser removidos.
# -Pnative: Ativa o perfil nativo para compilação GraalVM.
# -DskipTests: Pula a execução dos testes.
# -Dspring-boot.build-image.skip=true: ESSENCIAL - Diz ao plugin Spring Boot para NÃO construir a imagem Docker.
RUN ./mvnw clean package -Pnative -DskipTests -Dspring-boot.build-image.skip=true

# Estágio de execução: Usa uma imagem base mínima para rodar o executável nativo
FROM alpine/glibc

# Define o diretório de trabalho
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
