```docker exec -it schema-registry bash```

```docker cp src/main/resources/avro/PolicyReceivedEvent.avsc schema-registry:/tmp/PolicyReceivedEvent.avsc
docker cp src/main/resources/avro/PaymentProcessedEvent.avsc schema-registry:/tmp/PaymentProcessedEvent.avsc
docker cp src/main/resources/avro/SubscriptionProcessedEvent.avsc schema-registry:/tmp/SubscriptionProcessedEvent.avsc```

```kafka-avro-console-producer \
  --broker-list kafka:9092 \
  --topic payment-confirmed-events-topic \
  --property key.serializer=org.apache.kafka.common.serialization.StringSerializer \
  --property schema.registry.url=http://schema-registry:8081 \
  --property value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy \
  --property value.schema.file=/tmp/PaymentProcessedEvent.avsc \
  --property parse.key=true \
  --property key.separator=: \
  --property headers=eventType:PAYMENT_PROCESSED,eventSourceId:test-source \
  --property print.key=true \
  --property print.value=true \
  --property print.headers=true
```

```policy-10e3110b:{"policyId": "33781e7d-94a5-46b6-93f9-a436b5f65d28", "paymentTransactionId": "TRANS123456789", "status": "CONFIRMED", "amount": "100.00", "paymentDate":"2025-06-10"}```

Para apagar (ou, mais precisamente, truncar) mensagens de um tópico no Kafka, você tem algumas opções. É importante entender que, no Kafka, as mensagens não são realmente "apagadas" individualmente no broker da mesma forma que em um banco de dados. Elas expiram com base em políticas de retenção de tempo ou tamanho do log, ou são truncadas a partir de um determinado offset.

Aqui estão os métodos mais comuns para "apagar" mensagens:

1. Alterar a Política de Retenção do Tópico (Recomendado para "limpar" o tópico)
   Esta é a forma mais comum de garantir que as mensagens antigas sejam removidas automaticamente. Você pode definir um tempo de retenção muito curto para forçar a remoção rápida.

Passos:  

a.  Verifique a política de retenção atual do tópico:
bash kafka-configs --bootstrap-server localhost:9092 --describe --entity-type topics --entity-name <nome_do_topico>
(Substitua localhost:9092 pela URL do seu broker Kafka se for diferente).

b.  Altere a política de retenção para um tempo curto (ex: 1 minuto = 60000 ms):
bash kafka-configs --bootstrap-server localhost:9092 --alter --entity-type topics --entity-name <nome_do_topico> --add-config retention.ms=60000
* Após alguns minutos (ou o tempo de retenção definido), o Kafka vai apagar as mensagens mais antigas que 1 minuto.

c.  Se quiser resetar para o padrão depois:
bash kafka-configs --bootstrap-server localhost:9092 --alter --entity-type topics --entity-name <nome_do_topico> --delete-config retention.ms


echo 'policy-10e3110b:{"policyId": "33781e7d-94a5-46b6-93f9-a436b5f65d28", "paymentTransactionId": "TRANS123456789", "status": "CONFIRMED", "amount": "100.00", "paymentDate":"2025-06-10"}' | kafka-avro-console-producer --broker-list kafka:9092 --topic payment-confirmed-events-topic --property key.serializer=org.apache.kafka.common.serialization.StringSerializer --property schema.registry.url=http://schema-registry:8081 --property value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy --property value.schema.file=/tmp/PaymentProcessedEvent.avsc --property parse.key=true --property key.separator=: --property headers=eventType:PAYMENT_PROCESSED,eventSourceId:test-source --property print.key=true --property print.value=true --property print.headers=true


docker exec kafka kafka-topics --bootstrap-server kafka:9092 --create --topic payment-confirmed-events-topic --partitions 1 --replication-factor 1


docker exec schema-registry bash -c 'echo '"'"'policy-33781e7d:{"policyId": "33781e7d-94a5-46b6-93f9-a436b5f65d28", "paymentTransactionId": "TRANS123456789", "status": "CONFIRMED", "amount": "100.00", "paymentDate":"2025-06-10", "eventType": "PAYMENT_PROCESSED", "eventSourceId": "my-test-source"}'"'"' | kafka-avro-console-producer --broker-list kafka:9092 --topic payment-confirmed-events-topic --property key.serializer=org.apache.kafka.common.serialization.StringSerializer --property schema.registry.url=http://schema-registry:8081 --property value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy --property value.schema.file=/tmp/PaymentProcessedEvent.avsc --property parse.key=true --property key.separator=: --property print.key=true --property print.value=true'


```docker exec schema-registry bash -c 'echo '"'"'policy-33781e7d:{"policyId": "fd251c53-e12e-41c9-90d0-5223e722632b", "paymentTransactionId": "TRANS123456789", "status": "CONFIRMED", "amount": "100.00", "paymentDate":"2025-06-10", "eventType": {"string": "PAYMENT_PROCESSED"}, "eventSourceId": {"string": "my-test-source"}}'"'"' | kafka-avro-console-producer --broker-list kafka:9092 --topic payment-confirmed-events-topic --property key.serializer=org.apache.kafka.common.serialization.StringSerializer --property schema.registry.url=http://schema-registry:8081 --property value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy --property value.schema.file=/tmp/PaymentProcessedEvent.avsc --property parse.key=true --property key.separator=: --property print.key=true --property print.value=true'```

```docker exec schema-registry bash -c 'echo '"'"'policy-33781e7d:{"policyId": "fd8ccf1c-ac7a-43a4-9296-8063d48f81b2", "paymentTransactionId": "TRANS123456789", "status": "DENIED", "amount": "100.00", "paymentDate":"2025-06-10", "eventType": {"string": "PAYMENT_PROCESSED"}, "eventSourceId": {"string": "my-test-source"}}'"'"' | kafka-avro-console-producer --broker-list kafka:9092 --topic payment-confirmed-events-topic --property key.serializer=org.apache.kafka.common.serialization.StringSerializer --property schema.registry.url=http://schema-registry:8081 --property value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy --property value.schema.file=/tmp/PaymentProcessedEvent.avsc --property parse.key=true --property key.separator=: --property print.key=true --property print.value=true'```


docker cp src/main/resources/avro/SubscriptionProcessedEvent.avsc schema-registry:/tmp/SubscriptionProcessedEvent.avsc

docker exec schema-registry bash -c 'echo '"'"'policy-sub-123:{"policyId": "fd251c53-e12e-41c9-90d0-5223e722632b", "status": "ACTIVE", "subscriptionId": "SUB-XYZ-789", "processedDate":"2025-06-10", "eventType": {"string": "SUBSCRIPTION_PROCESSED"}, "eventSourceId": {"string": "my-subscription-source"}}'"'"' | kafka-avro-console-producer --broker-list kafka:9092 --topic subscription-authorized-events-topic --property key.serializer=org.apache.kafka.common.serialization.StringSerializer --property schema.registry.url=http://schema-registry:8081 --property value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy --property value.schema.file=/tmp/SubscriptionProcessedEvent.avsc --property parse.key=true --property key.separator=: --property print.key=true --property print.value=true'
