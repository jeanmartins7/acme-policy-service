package com.acmeinsurance.order.infrastructure.kafka.consumer;

import com.acmeinsurance.order.avro.SubscriptionProcessedEvent;
import com.acmeinsurance.order.domain.service.SubscriptionProcessedService;
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
public class SubscriptionEventListener {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionEventListener.class);

    private final SubscriptionProcessedService subscriptionProcessedService;

    private static final String SUBSCRIPTION_TOPIC = "${policy.kafka.topics.subscription-events}";
    private static final String CONSUMER_GROUP_ID = "${spring.kafka.consumer.group-id}";

    @KafkaListener(topics = SUBSCRIPTION_TOPIC, groupId = CONSUMER_GROUP_ID, containerFactory = "kafkaListenerContainerFactory")
    public void listenSubscriptionProcessedEvent(
            @Payload SubscriptionProcessedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.OFFSET) Long offset,
            @Header("eventType") String eventType,
            @Header("eventSourceId") String eventSourceId,
            Acknowledgment acknowledgment) {

        log.info("Received message from topic '{}', key: '{}', offset: {}, eventType: '{}', Avro Subscription ID: {}",
                SUBSCRIPTION_TOPIC, key, offset, eventType, event.getPolicyId());

        if (!"SUBSCRIPTION_PROCESSED".equals(eventType)) {
            log.warn("Discarding message from topic '{}' with non-matching eventType '{}'. Expected: 'SUBSCRIPTION_PROCESSED'. Key: {}",
                    SUBSCRIPTION_TOPIC, eventType, key);
            acknowledgment.acknowledge();
            return;
        }

        if (!(event instanceof SubscriptionProcessedEvent)) {
            log.error("Received unexpected payload type for eventType '{}'. Expected SubscriptionProcessedEvent. Payload type: {}. Key: {}",
                    eventType, event.getClass().getName(), key);
            acknowledgment.acknowledge();
            return;
        }

        subscriptionProcessedService.processSubscription(event)
                .doOnSuccess(policyRequest -> {
                    log.info("Subscription processing completed for policyId: {}. New status: {}", policyRequest.getId(), policyRequest.getStatus());
                    acknowledgment.acknowledge();
                })
                .doOnError(e -> {
                    log.error("Error processing subscription for policyId {} [key: {}, offset: {}]: {}", event.getPolicyId(), key, offset, e.getMessage());

                })
                .subscribe();
    }
}