#!/bin/bash

UUID_REGEX="[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
TOPIC_NAME="payment-confirmed-events-topic"
SCHEMA_FILE="../avro/PaymentProcessedEvent.avsc"
SCHEMA_TARGET_PATH="/tmp/PaymentProcessedEvent.avsc"

is_valid_uuid() {
  [[ "$1" =~ ^$UUID_REGEX$ ]]
}

setup_kafka_environment() {
  echo "Iniciando preparação do ambiente Kafka..."

  echo "Verificando a existência do tópico Kafka: $TOPIC_NAME"
  TOPIC_EXISTS=$(docker exec kafka kafka-topics --bootstrap-server kafka:9092 --list 2>/dev/null | grep -w "$TOPIC_NAME")

  if [ -z "$TOPIC_EXISTS" ]; then
    echo "Tópico '$TOPIC_NAME' não encontrado. Criando..."
    docker exec kafka kafka-topics --bootstrap-server kafka:9092 --create --topic "$TOPIC_NAME" --partitions 1 --replication-factor 1 >/dev/null 2>&1
    if [ $? -eq 0 ]; then
      echo "Tópico '$TOPIC_NAME' criado com sucesso."
    else
      echo "Erro ao criar o tópico '$TOPIC_NAME'. Abortando."
      exit 1
    fi
  else
    echo "Tópico '$TOPIC_NAME' já existe."
  fi

  echo "Copiando o schema '$SCHEMA_FILE' para o container schema-registry..."
  if [ -f "$SCHEMA_FILE" ]; then
    docker cp "$SCHEMA_FILE" schema-registry:"$SCHEMA_TARGET_PATH" >/dev/null 2>&1
    if [ $? -eq 0 ]; then
      echo "Schema copiado com sucesso para schema-registry:$SCHEMA_TARGET_PATH."
    else
      echo "Erro ao copiar o schema. Abortando."
      exit 1
    fi
  else
    echo "Erro: Arquivo de schema '$SCHEMA_FILE' não encontrado. Abortando."
    exit 1
  fi
  echo "Preparação do ambiente concluída."
  echo "---"
}