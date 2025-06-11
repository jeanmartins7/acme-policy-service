#!/bin/bash

source ./subscription_common_setup.sh

if ! command -v setup_subscription_kafka_environment &> /dev/null
then
    echo "Erro: Não foi possível carregar as funções comuns de setup de assinatura. Verifique o caminho de 'subscription_common_setup.sh'."
    exit 1
fi

setup_subscription_kafka_environment

echo "Iniciando processamento de ativação de assinaturas..."

if [ -z "$1" ]; then
  echo "Uso: $0 <lista_de_uuids_separados_por_virgula>"
  echo "Exemplo: $0 ffdff746-36d5-4f61-8d49-a4c6affcd332,another-uuid-here"
  exit 1
fi

IFS=',' read -ra UUID_LIST_RAW <<< "$1"

for raw_uuid in "${UUID_LIST_RAW[@]}"; do
  clean_uuid=$(echo "$raw_uuid" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' | sed -e 's/^"//' -e 's/"$//' -e "s/^'//" -e "s/'$//")

  if is_valid_uuid "$clean_uuid"; then
    echo "Processando UUID para ativação: $clean_uuid"
    SUBSCRIPTION_STATUS="ACTIVE"
    current_date=$(date +%Y-%m-%d)
    PRODUCER_COMMAND="echo 'policy-sub-123:{\"policyId\": \"$clean_uuid\", \"status\": \"$SUBSCRIPTION_STATUS\", \"subscriptionId\": \"SUB-XYZ-789\", \"processedDate\":\"$current_date\", \"eventType\": {\"string\": \"SUBSCRIPTION_PROCESSED\"}, \"eventSourceId\": {\"string\": \"my-subscription-source\"}}' | kafka-avro-console-producer --broker-list kafka:9092 --topic $TOPIC_NAME --property key.serializer=org.apache.kafka.common.serialization.StringSerializer --property schema.registry.url=http://schema-registry:8081 --property value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy --property value.schema.file=$SCHEMA_TARGET_PATH --property parse.key=true --property key.separator=: --property print.key=true --property print.value=true"
    docker exec schema-registry bash -c "$PRODUCER_COMMAND" >/dev/null 2>&1
    if [ $? -eq 0 ]; then
      echo "Assinatura $clean_uuid ATIVADA com sucesso."
    else
      echo "Erro ao ativar assinatura $clean_uuid."
    fi
  else
    echo "Aviso: UUID inválido ou não encontrado na entrada: '$raw_uuid'. Ignorando."
  fi
done

echo "Processamento de ativação de assinaturas concluído."