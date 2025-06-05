# Usa uma imagem base Java 17 leve para aplicações Spring Boot
FROM eclipse-temurin:17-jdk-jammy

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo JAR da sua aplicação para o contêiner
# Assumimos que o Maven irá gerar um JAR na pasta 'target' com o nome policy-service-0.0.1-SNAPSHOT.jar
# (ou o nome do seu projeto, ex: acme-policy-service-0.0.1-SNAPSHOT.jar)
# É importante que o nome do JAR aqui corresponda ao que o Maven gera.
COPY target/policy-service-*.jar app.jar

# Expõe a porta que sua aplicação Spring Boot escutará
EXPOSE 8081

# Comando para executar a aplicação quando o contêiner for iniciado
ENTRYPOINT ["java", "-jar", "app.jar"]