package com.acmeinsurance.order.infrastructure.config;

import com.acmeinsurance.order.avro.PaymentProcessedEvent;
import com.acmeinsurance.order.avro.PolicyReceivedEvent;
import com.acmeinsurance.order.avro.SubscriptionProcessedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.avro.AvroRuntimeException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private static final String SPRING_KAFKA_BOOTSTRAP_SERVERS = "spring.kafka.bootstrap-servers";
    private static final String SPRING_KAFKA_CONSUMER_GROUP_ID = "spring.kafka.consumer.group-id";
    private static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
    private static final String EARLIEST = "earliest";
    private static final String FALSE = "false";
    private final Environment env;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Value("${policy.kafka.topics.dead-letter-queue}")
    private String dlqTopicName;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getProperty(SPRING_KAFKA_BOOTSTRAP_SERVERS));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, env.getProperty(SPRING_KAFKA_CONSUMER_GROUP_ID));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(SCHEMA_REGISTRY_URL, env.getProperty("spring.kafka.properties.schema.registry.url", "http://localhost:8081"));
        props.put("value.subject.name.strategy", RecordNameStrategy.class.getName());
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, FALSE);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public DeadLetterPublishingRecoverer publisherRecoverer() {

        return new DeadLetterPublishingRecoverer(kafkaTemplate, (r, e) ->
                new TopicPartition(dlqTopicName, 0));
    }

    @Bean
    public DefaultErrorHandler errorHandler() {

        final DefaultErrorHandler errorHandler = new DefaultErrorHandler(publisherRecoverer(), new FixedBackOff(2000L, 2L));

        errorHandler.addNotRetryableExceptions(
                SerializationException.class,
                RestClientException.class,
                AvroRuntimeException.class,
                JsonProcessingException.class
        );

        return errorHandler;

    }

    @Bean
    public RecordFilterStrategy<String, Object> myMessageFilterStrategy() {
        return consumerRecord -> !(consumerRecord.value() instanceof PolicyReceivedEvent) &&
                !(consumerRecord.value() instanceof PaymentProcessedEvent) &&
                !(consumerRecord.value() instanceof SubscriptionProcessedEvent);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            final DefaultErrorHandler errorHandler,
            @Qualifier("myMessageFilterStrategy") RecordFilterStrategy<String, Object> filterStrategy) {

        final ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setSyncCommits(false);
        factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(errorHandler);
        factory.setRecordFilterStrategy(filterStrategy);
        return factory;
    }
}