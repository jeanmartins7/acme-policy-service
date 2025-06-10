package com.acmeinsurance.order.infrastructure.kafka.consumer;

import com.acmeinsurance.order.avro.PolicyReceivedEvent;
import com.acmeinsurance.order.domain.usecase.ProcessFraudAnalysisUseCase;
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

    private final ProcessFraudAnalysisUseCase processFraudAnalysisUseCase;

    private static final String POLICY_RECEIVED_TOPIC = "${policy.kafka.topics.status-notifications}";
    private static final String CONSUMER_GROUP_ID = "${spring.kafka.consumer.group-id}";

    @KafkaListener(topics = POLICY_RECEIVED_TOPIC, groupId = CONSUMER_GROUP_ID, containerFactory = "kafkaListenerContainerFactory")
    public void listenPolicyReceivedEvent(
            @Payload PolicyReceivedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.OFFSET) Long offset,
            @Header("eventType") String eventType,
            @Header("eventSourceId") String eventSourceId,
            Acknowledgment acknowledgment) {

        log.info("Received message from topic '{}', key: '{}', offset: {}, eventType: '{}', eventSourceId: '{}', Avro Policy ID: {}",
                POLICY_RECEIVED_TOPIC, key, offset, eventType, eventSourceId, event.getPolicyId());

        if (!"RECEIVED".equals(eventType)) {
            log.warn("Discarding message from topic '{}' with non-matching eventType '{}'. Expected: 'POLICY_RECEIVED'. Key: {}",
                    POLICY_RECEIVED_TOPIC, eventType, key);
            acknowledgment.acknowledge();
            return;
        }

        if (!(event instanceof PolicyReceivedEvent)) {
            log.error("Received unexpected payload type for eventType '{}'. Expected PolicyReceivedEvent. Payload type: {}. Key: {}",
                    eventType, event.getClass().getName(), key);
            acknowledgment.acknowledge();
            return;
        }

        processFraudAnalysisUseCase.execute(event.getPolicyId())
                .doOnSuccess(policyRequest -> {
                    log.info("Fraud analysis completed for policyId: {}. New status: {}", policyRequest.getId(), policyRequest.getStatus());
                    acknowledgment.acknowledge();
                })
                .doOnError(e -> {
                    log.error("Error processing fraud analysis for policyId {} [key: {}, offset: {}]: {}", event.getPolicyId(), key, offset, e.getMessage());

                })
                .subscribe();
    }
}