package com.acmeinsurance.order.infrastructure.kafka.consumer;

import com.acmeinsurance.order.avro.PaymentProcessedEvent;
import com.acmeinsurance.order.domain.service.PaymentProcessedService;
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
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final PaymentProcessedService paymentProcessedService;

    private static final String PAYMENT_TOPIC = "${policy.kafka.topics.payment-events}";
    private static final String CONSUMER_GROUP_ID = "${spring.kafka.consumer.group-id}";

    @KafkaListener(topics = PAYMENT_TOPIC, groupId = CONSUMER_GROUP_ID, containerFactory = "kafkaListenerContainerFactory")
    public void listenPaymentProcessedEvent(
                                             @Payload PaymentProcessedEvent event,
                                             @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                             @Header(KafkaHeaders.OFFSET) Long offset,
                                             @Header("eventType") String eventType,
                                             @Header("eventSourceId") String eventSourceId,
                                             Acknowledgment acknowledgment) {

        log.info("Received message from topic '{}', key: '{}', offset: {}, eventType: '{}', Avro Payment ID: {}",
                PAYMENT_TOPIC, key, offset, eventType, event.getPolicyId());

        if (!"PAYMENT_PROCESSED".equals(eventType)) {
            log.warn("Discarding message from topic '{}' with non-matching eventType '{}'. Expected: 'PAYMENT_PROCESSED'. Key: {}",
                    PAYMENT_TOPIC, eventType, key);
            acknowledgment.acknowledge();
            return;
        }

        if (!(event instanceof PaymentProcessedEvent)) {
            log.error("Received unexpected payload type for eventType '{}'. Expected PaymentProcessedEvent. Payload type: {}. Key: {}",
                    eventType, event.getClass().getName(), key);
            acknowledgment.acknowledge();
            return;
        }

        paymentProcessedService.processPayment(event)
                .doOnSuccess(policyRequest -> {
                    log.info("Payment processing completed for policyId: {}. New status: {}", policyRequest.getId(), policyRequest.getStatus());
                    acknowledgment.acknowledge();
                })
                .doOnError(e -> {
                    log.error("Error processing payment for policyId {} [key: {}, offset: {}]: {}", event.getPolicyId(), key, offset, e.getMessage());

                })
                .subscribe();
    }
}