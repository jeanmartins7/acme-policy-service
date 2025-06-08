package com.acmeinsurance.policy.infrastructure.kafka;

import com.acmeinsurance.policy.avro.PolicyReceivedEvent;
import com.acmeinsurance.policy.domain.usecase.ProcessFraudAnalysisUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PolicyReceivedEventListener {

    private static final Logger log = LoggerFactory.getLogger(PolicyReceivedEventListener.class);

    private final ObjectMapper objectMapper;
    private final ProcessFraudAnalysisUseCase processFraudAnalysisUseCase;

    private static final String KAFKA_LISTENER_CONTAINER_FACTORY = "kafkaListenerContainerFactory";
    private static final String CONSUMER_GROUP_ID = "${spring.kafka.consumer.group-id}";
    public static final String POLICY_KAFKA_TOPICS_STATUS_NOTIFICATIONS = "${policy.kafka.topics.status-notifications}";


    @KafkaListener(topics = POLICY_KAFKA_TOPICS_STATUS_NOTIFICATIONS, groupId = CONSUMER_GROUP_ID, containerFactory = KAFKA_LISTENER_CONTAINER_FACTORY)
    public void listenPolicyReceivedEvent(
            @Payload String messageJson,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.OFFSET) Long offset,
            @Header("eventType") String eventType,
            @Header("eventSourceId") String eventSourceId,
            Acknowledgment acknowledgment) {

        log.info("Received message from topic '{}', key: '{}', offset: {}, eventType: '{}', eventSourceId: '{}'",
                POLICY_KAFKA_TOPICS_STATUS_NOTIFICATIONS, key, offset, eventType, eventSourceId);

        if (!"POLICY_RECEIVED".equals(eventType)) {
            log.warn("Discarding message from topic '{}' with unknown eventType '{}'. Key: {}", POLICY_KAFKA_TOPICS_STATUS_NOTIFICATIONS, eventType, key);
            acknowledgment.acknowledge();
            return;
        }

        try {
            PolicyReceivedEvent event = objectMapper.readValue(messageJson, PolicyReceivedEvent.class);
            log.info("Deserialized PolicyReceivedEvent for policyId: {}", event.getPolicyId());

            processFraudAnalysisUseCase.execute(event.getPolicyId())
                    .doOnSuccess(policyRequest -> {
                        log.info("Fraud analysis completed for policyId: {}. New status: {}", policyRequest.getId(), policyRequest.getStatus());
                        acknowledgment.acknowledge();
                    })
                    .doOnError(e -> log.error("Error processing fraud analysis for policyId {} [key: {}, offset: {}]: {}", event.getPolicyId(), key, offset, e.getMessage()))
                    .subscribe();
        } catch (JsonProcessingException e) {
            log.error("Error deserializing message for key {}: {}", key, e.getMessage());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Unexpected error during PolicyReceivedEvent processing for key {}: {}", key, e.getMessage());
        }
    }
}