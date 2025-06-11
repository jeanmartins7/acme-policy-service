package com.acmeinsurance.order.infrastructure.kafka.consumer;

import com.acmeinsurance.order.avro.PolicyReceivedEvent;
import com.acmeinsurance.order.domain.usecase.ProcessFraudAnalysisUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
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

    private static final String POLICY_RECEIVED_EVENT_TYPE = "RECEIVED";
    private static final String LOG_TOPIC_PROPERTY = "${policy.kafka.topics.status-notifications}";
    private static final String LOG_RECEIVED_MESSAGE = "Received message from topic '{}', key: '{}', offset: {}, eventType: '{}', eventSourceId: '{}', Avro Policy ID: {}";
    private static final String LOG_DISCARDING_EVENT_TYPE_MISMATCH = "Discarding message from topic '{}' with non-matching eventType '{}'. Expected: '{}'. Key: {}";
     private static final String LOG_FRAUD_ANALYSIS_COMPLETED = "Fraud analysis completed for policyId: {}. New status: {}";
    private static final String LOG_FRAUD_ANALYSIS_ERROR = "Error processing fraud analysis for policyId {} [key: {}, offset: {}]: {}";

    private final ProcessFraudAnalysisUseCase processFraudAnalysisUseCase;
    private final Environment env;

    @KafkaListener(topics = LOG_TOPIC_PROPERTY, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void listenPolicyReceivedEvent(
            @Payload PolicyReceivedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.OFFSET) Long offset,
            @Header("eventType") String eventType,
            @Header("eventSourceId") String eventSourceId,
            Acknowledgment acknowledgment) {

        log.info(LOG_RECEIVED_MESSAGE, env.getProperty(LOG_TOPIC_PROPERTY), key, offset, eventType, eventSourceId, event.getPolicyId());

        if (!POLICY_RECEIVED_EVENT_TYPE.equals(eventType)) {
            log.warn(LOG_DISCARDING_EVENT_TYPE_MISMATCH, env.getProperty(LOG_TOPIC_PROPERTY), eventType, POLICY_RECEIVED_EVENT_TYPE, key);
            acknowledgment.acknowledge();
            return;
        }
        //TODO idempotency
        processFraudAnalysisUseCase.execute(event.getPolicyId())
                .doOnSuccess(policyRequest -> {
                    log.info(LOG_FRAUD_ANALYSIS_COMPLETED, policyRequest.getId(), policyRequest.getStatus());
                    acknowledgment.acknowledge();
                })//TODO DLQ
                .doOnError(e -> log.error(LOG_FRAUD_ANALYSIS_ERROR, event.getPolicyId(), key, offset, e.getMessage(), e))
                .subscribe();
    }
}