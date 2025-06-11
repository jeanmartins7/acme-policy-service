#!/bin/bash

source ./kafka_common_setup.sh

if ! command -v setup_kafka_environment &> /dev/null
then
    echo "Erro: Não foi possível carregar as funções comuns de setup. Verifique o caminho de 'kafka_common_setup.sh'."
    exit 1
fi

setup_kafka_environment

echo "Iniciando processamento de recusa de pagamentos..."

if [ -z "$1" ]; then
  echo "Uso: $0 <lista_de_uuids_separados_por_virgula>"
  echo "Exemplo: $0 ffdff746-36d5-4f61-8d49-a4c6affcd332,another-uuid-here"
  exit 1
fi


IFS=',' read -ra UUID_LIST_RAW <<< "$1"

for raw_uuid in "${UUID_LIST_RAW[@]}"; do

  clean_uuid=$(echo "$raw_uuid" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' | sed -e 's/^"//' -e 's/"$//' -e "s/^'//" -e "s/'$//")

  if is_valid_uuid "$clean_uuid"; then
    echo "Processando UUID para recusa: $clean_uuid"
    PAYMENT_STATUS="DENIED"
    PRODUCER_COMMAND="echo 'policy-33781e7d:{\"policyId\": \"$clean_uuid\", \"paymentTransactionId\": \"TRANS123456789\", \"status\": \"$PAYMENT_STATUS\", \"amount\": \"100.00\", \"paymentDate\":\"2025-06-10\", \"eventType\": {\"string\": \"PAYMENT_PROCESSED\"}, \"eventSourceId\": {\"string\": \"my-test-source\"}}' | kafka-avro-console-producer --broker-list kafka:9092 --topic payment-confirmed-events-topic --property key.serializer=org.apache.kafka.kafka.common.serialization.StringSerializer --property schema.registry.url=http://schema-registry:8081 --property value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy --property value.schema.file=/tmp/PaymentProcessedEvent.avsc --property parse.key=true --property key.separator=: --property print.key=true --property print.value=true"
    docker exec schema-registry bash -c "$PRODUCER_COMMAND" >/dev/null 2>&1
    if [ $? -eq 0 ]; then
      echo "UUID $clean_uuid recusado com sucesso."
    else
      echo "Erro ao recusar UUID $clean_uuid."
    fi
  else
    echo "Aviso: UUID inválido ou não encontrado na entrada: '$raw_uuid'. Ignorando."
  fi
done

echo "Processamento de recusa concluído."