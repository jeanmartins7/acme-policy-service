#!/bin/bash

MONITORAR_ARQUIVO="nao"
ARQUIVO_LOG=""
UUID_REGEX="[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
TOPIC_NAME="payment-confirmed-events-topic"
SCHEMA_FILE="../avro/PaymentProcessedEvent.avsc" # Caminho relativo ao script
SCHEMA_TARGET_PATH="/tmp/PaymentProcessedEvent.avsc"

is_valid_uuid() {
  [[ "$1" =~ ^$UUID_REGEX$ ]]
}

process_uuid_and_status() {
  local found_uuid="$1"
  local decision_input="$2"
  local payment_status=""

  if [ "$decision_input" = "1" ]; then
    payment_status="CONFIRMED"
  elif [ "$decision_input" = "2" ]; then
    payment_status="DENIED"
  else
    echo "Decisão inválida. Use 1 para aprovar ou 2 para negar. Nenhuma ação será tomada para o UUID: $found_uuid"
    return 1
  fi

  echo ""
  echo "UUID recebido: $found_uuid"
  echo "Status de pagamento definido como: $payment_status"

  PRODUCER_COMMAND="echo 'policy-33781e7d:{\"policyId\": \"$found_uuid\", \"paymentTransactionId\": \"TRANS123456789\", \"status\": \"$payment_status\", \"amount\": \"100.00\", \"paymentDate\":\"2025-06-10\", \"eventType\": {\"string\": \"PAYMENT_PROCESSED\"}, \"eventSourceId\": {\"string\": \"my-test-source\"}}' | kafka-avro-console-producer --broker-list kafka:9092 --topic payment-confirmed-events-topic --property key.serializer=org.apache.kafka.common.serialization.StringSerializer --property schema.registry.url=http://schema-registry:8081 --property value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy --property value.schema.file=/tmp/PaymentProcessedEvent.avsc --property parse.key=true --property key.separator=: --property print.key=true --property print.value=true"
  docker exec schema-registry bash -c "$PRODUCER_COMMAND"
  echo "Mensagem Kafka enviada para o UUID $found_uuid com status $payment_status. Aguardando o próximo..."
}

echo "Iniciando programa de automação de UUID para Kafka."

echo "Verificando a existência do tópico Kafka: $TOPIC_NAME"
TOPIC_EXISTS=$(docker exec kafka kafka-topics --bootstrap-server kafka:9092 --list | grep -w "$TOPIC_NAME")

if [ -z "$TOPIC_EXISTS" ]; then
  echo "Tópico '$TOPIC_NAME' não encontrado. Criando..."
  docker exec kafka kafka-topics --bootstrap-server kafka:9092 --create --topic "$TOPIC_NAME" --partitions 1 --replication-factor 1
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
  docker cp "$SCHEMA_FILE" schema-registry:"$SCHEMA_TARGET_PATH"
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

echo ""

if [ "$MONITORAR_ARQUIVO" = "sim" ]; then
  echo "Monitorando o arquivo: $ARQUIVO_LOG por um UUID (em loop)..."
  if [ -f "$ARQUIVO_LOG" ]; then
    tail -f "$ARQUIVO_LOG" | while IFS= read -r line; do
      if [[ "$line" =~ $UUID_REGEX ]]; then
        found_uuid="${BASH_REMATCH[0]}"
        if is_valid_uuid "$found_uuid"; then
          read -p "UUID encontrado na linha: '$line'. Deseja aprovar (1) ou negar (2) o pagamento? " decision
          process_uuid_and_status "$found_uuid" "$decision"
        fi
      fi
    done
  else
    echo "Erro: Arquivo de log '$ARQUIVO_LOG' não encontrado."
    exit 1
  fi
else
  echo "Aguardando um UUID na entrada padrão (em loop). Digite uma linha que contenha um UUID, e em seguida a decisão (1 para aprovar, 2 para negar)."
  echo "Exemplo de entrada do UUID: ffdff746-36d5-4f61-8d49-a4c6affcd332"
  echo "Exemplo com aspas: \"ffdff746-36d5-4f61-8d49-a4c6affcd332\""
  echo "Exemplo com texto misturado: Log de evento: UUID processado ffdff746-36d5-4f61-8d49-a4c6affcd332 finalizado."
  while IFS= read -r line; do
    if [[ "$line" =~ $UUID_REGEX ]]; then
      found_uuid="${BASH_REMATCH[0]}"
      if is_valid_uuid "$found_uuid"; then
        read -p "Decisão para o UUID '$found_uuid' (1 para aprovar, 2 para negar): " decision_input
        process_uuid_and_status "$found_uuid" "$decision_input"
      else
        echo "Nenhum UUID válido encontrado na linha: '$line'. Tentando novamente..."
      fi
    else
      echo "Nenhum UUID válido encontrado na linha: '$line'. Tentando novamente..."
    fi
  done
fi