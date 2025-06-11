package com.acmeinsurance.order.infrastructure.kafka.consumer;

import com.acmeinsurance.order.avro.PaymentProcessedEvent;
import com.acmeinsurance.order.domain.service.PaymentProcessedService;
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
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private static final String PAYMENT_PROCESSED_EVENT_TYPE = "PAYMENT_PROCESSED";
    private static final String LOG_TOPIC_PROPERTY = "${policy.kafka.topics.payment-events}";
    private static final String LOG_MISSING_EVENT_TYPE = "Received message with missing 'eventType' field in Avro payload. Key: {}";
    private static final String LOG_RECEIVED_EVENT = "Received event for Policy ID: {}. Key: {}. Offset: {}. EventType: {}. Topic: {}";
    private static final String LOG_DISCARDING_EVENT = "Discarding event for Policy ID: {} with non-matching EventType: {}. Expected: '{}'.";
    private static final String LOG_PROCESSING_COMPLETED = "Payment processing completed for Policy ID: {}. New status: {}";
    private static final String LOG_PROCESSING_ERROR = "Error processing payment for Policy ID: {} (Key: {}, Offset: {}): {}";

    private final PaymentProcessedService paymentProcessedService;
    private final Environment env;

    @KafkaListener(topics = LOG_TOPIC_PROPERTY, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void listenPaymentProcessedEvent(
            @Payload PaymentProcessedEvent event,
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

    private Mono<Void> processValidEvent(final PaymentProcessedEvent event, final String key, final Long offset, final String eventType, final Acknowledgment acknowledgment) {
        log.info(LOG_RECEIVED_EVENT, event.getPolicyId(), key, offset, eventType, env.getProperty(LOG_TOPIC_PROPERTY));

        if (!PAYMENT_PROCESSED_EVENT_TYPE.equals(eventType)) {
            log.warn(LOG_DISCARDING_EVENT, event.getPolicyId(), eventType, PAYMENT_PROCESSED_EVENT_TYPE);
            acknowledgment.acknowledge();
            return Mono.empty();
        }
        //TODO idempotency
        return paymentProcessedService.processPayment(event)
                .doOnSuccess(policyRequest -> {
                    log.info(LOG_PROCESSING_COMPLETED, policyRequest.getId(), policyRequest.getStatus());
                    acknowledgment.acknowledge();
                })//TODO DLQ
                .doOnError(e -> log.error(LOG_PROCESSING_ERROR, event.getPolicyId(), key, offset, e.getMessage(), e))
                .then();
    }
}