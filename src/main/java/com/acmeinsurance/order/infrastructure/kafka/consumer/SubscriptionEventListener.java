package com.acmeinsurance.order.infrastructure.kafka.consumer;

import com.acmeinsurance.order.avro.SubscriptionProcessedEvent;
import com.acmeinsurance.order.domain.service.SubscriptionProcessedService;
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
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriptionEventListener {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionEventListener.class);

    private static final String SUBSCRIPTION_PROCESSED_EVENT_TYPE = "SUBSCRIPTION_PROCESSED";
    private static final String LOG_TOPIC_PROPERTY = "${policy.kafka.topics.subscription-events}";
    private static final String LOG_MISSING_EVENT_TYPE = "Received message with missing 'eventType' field in Avro payload. Key: {}";
    private static final String LOG_RECEIVED_MESSAGE = "Received message from topic '{}', key: '{}', offset: {}, eventType: '{}', Avro Subscription ID: {}";
    private static final String LOG_DISCARDING_EVENT = "Discarding message from topic '{}' with non-matching eventType '{}'. Expected: '{}'. Key: {}";
    private static final String LOG_PROCESSING_COMPLETED = "Subscription processing completed for policyId: {}. New status: {}";
    private static final String LOG_PROCESSING_ERROR = "Error processing subscription for policyId {} [key: {}, offset: {}]: {}";

    private final SubscriptionProcessedService subscriptionProcessedService;
    private final Environment env;

    @KafkaListener(topics = LOG_TOPIC_PROPERTY, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void listenSubscriptionProcessedEvent(
            @Payload SubscriptionProcessedEvent event,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.OFFSET) Long offset,
            Acknowledgment acknowledgment) {

        Optional.ofNullable(event.getEventType())
                .map(eventType -> processValidEvent(event, key, offset, eventType, acknowledgment))
                .orElseGet(() -> {
                    log.warn(LOG_MISSING_EVENT_TYPE, key);
                    acknowledgment.acknowledge();
                    return Mono.empty();
                })
                .subscribe();
    }

    private Mono<Void> processValidEvent(SubscriptionProcessedEvent event, String key, Long offset, String eventType, Acknowledgment acknowledgment) {
        log.info(LOG_RECEIVED_MESSAGE, env.getProperty(LOG_TOPIC_PROPERTY), key, offset, eventType, event.getPolicyId());

        if (!SUBSCRIPTION_PROCESSED_EVENT_TYPE.equals(eventType)) {
            log.warn(LOG_DISCARDING_EVENT, env.getProperty(LOG_TOPIC_PROPERTY), eventType, SUBSCRIPTION_PROCESSED_EVENT_TYPE, key);
            acknowledgment.acknowledge();
            return Mono.empty();
        }
        //TODO idempotency
        return subscriptionProcessedService.processSubscription(event)
                .doOnSuccess(policyRequest -> {
                    log.info(LOG_PROCESSING_COMPLETED, policyRequest.getId(), policyRequest.getStatus());
                    acknowledgment.acknowledge();
                })//TODO DLQ
                .doOnError(e -> log.error(LOG_PROCESSING_ERROR, event.getPolicyId(), key, offset, e.getMessage(), e))
                .then();
    }
}