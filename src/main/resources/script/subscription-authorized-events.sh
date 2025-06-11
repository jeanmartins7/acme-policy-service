#!/bin/bash

MONITORAR_ARQUIVO="nao"
ARQUIVO_LOG=""
UUID_REGEX="[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
TOPIC_NAME="subscription-authorized-events-topic"
SCHEMA_FILE="../avro/SubscriptionProcessedEvent.avsc"
SCHEMA_TARGET_PATH="/tmp/SubscriptionProcessedEvent.avsc"

is_valid_uuid() {
  [[ "$1" =~ ^$UUID_REGEX$ ]]
}

process_uuid_and_status() {
  local found_uuid="$1"
  local decision_input="$2"
  local subscription_status=""
  local current_date=$(date +%Y-%m-%d)

  if [ "$decision_input" = "1" ]; then
    subscription_status="ACTIVE"
  elif [ "$decision_input" = "2" ]; then
    subscription_status="INACTIVE"
  else
    echo "Decisão inválida. Use 1 para 'ACTIVE' ou 2 para 'INACTIVE'. Nenhuma ação será tomada para o UUID: $found_uuid"
    return 1
  fi

  echo ""
  echo "UUID recebido: $found_uuid"
  echo "Status da assinatura definido como: $subscription_status"


  PRODUCER_COMMAND="echo 'policy-sub-123:{\"policyId\": \"$found_uuid\", \"status\": \"$subscription_status\", \"subscriptionId\": \"SUB-XYZ-789\", \"processedDate\":\"$current_date\", \"eventType\": {\"string\": \"SUBSCRIPTION_PROCESSED\"}, \"eventSourceId\": {\"string\": \"my-subscription-source\"}}' | kafka-avro-console-producer --broker-list kafka:9092 --topic $TOPIC_NAME --property key.serializer=org.apache.kafka.common.serialization.StringSerializer --property schema.registry.url=http://schema-registry:8081 --property value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy --property value.schema.file=$SCHEMA_TARGET_PATH --property parse.key=true --property key.separator=: --property print.key=true --property print.value=true"

  docker exec schema-registry bash -c "$PRODUCER_COMMAND" >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Mensagem Kafka enviada para o UUID $found_uuid com status $subscription_status. Aguardando o próximo..."
  else
    echo "Erro ao enviar mensagem Kafka para o UUID $found_uuid."
  fi
}

echo "Iniciando programa de automação de UUID para Kafka (SubscriptionProcessedEvent)."

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

echo ""

if [ "$MONITORAR_ARQUIVO" = "sim" ]; then
  echo "Monitorando o arquivo: $ARQUIVO_LOG por um UUID (em loop)..."
  if [ -f "$ARQUIVO_LOG" ]; then
    tail -f "$ARQUIVO_LOG" | while IFS= read -r line; do
      if [[ "$line" =~ $UUID_REGEX ]]; then
        found_uuid="${BASH_REMATCH[0]}"
        if is_valid_uuid "$found_uuid"; then
          read -p "UUID encontrado na linha: '$line'. Deseja definir como ACTIVE (1) ou INACTIVE (2)? " decision
          process_uuid_and_status "$found_uuid" "$decision"
        fi
      fi
    done
  else
    echo "Erro: Arquivo de log '$ARQUIVO_LOG' não encontrado."
    exit 1
  fi
else
  echo "Aguardando um UUID na entrada padrão (em loop). Digite uma linha que contenha um UUID, e em seguida a decisão (1 para ACTIVE, 2 para INACTIVE)."
  echo "Exemplo de entrada do UUID: ffdff746-36d5-4f61-8d49-a4c6affcd332"
  echo "Exemplo com aspas: \"ffdff746-36d5-4f61-8d49-a4c6affcd332\""
  echo "Exemplo com texto misturado: Log de evento: UUID processado ffdff746-36d5-4f61-8d49-a4c6affcd332 finalizado."
  while IFS= read -r line; do
    if [[ "$line" =~ $UUID_REGEX ]]; then
      found_uuid="${BASH_REMATCH[0]}"
      if is_valid_uuid "$found_uuid"; then
        read -p "Decisão para o UUID '$found_uuid' (1 para ACTIVE, 2 para INACTIVE): " decision_input
        process_uuid_and_status "$found_uuid" "$decision_input"
      else
        echo "Nenhum UUID válido encontrado na linha: '$line'. Tentando novamente..."
      fi
    else
      echo "Nenhum UUID válido encontrado na linha: '$line'. Tentando novamente..."
    fi
  done
fi