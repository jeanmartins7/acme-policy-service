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

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final PaymentProcessedService paymentProcessedService;
    private final Environment env;

    @KafkaListener(topics = "${policy.kafka.topics.payment-events}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void listenPaymentProcessedEvent(
            @Payload PaymentProcessedEvent event,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.OFFSET) Long offset,
            Acknowledgment acknowledgment) {

        final String eventType = event.getEventType();

        if (eventType == null) {
            log.warn("Received message with missing 'eventType' field in Avro payload. Key: {}", key);
            acknowledgment.acknowledge();
            return;
        }

        log.info("Received message from topic '{}', key: '{}', offset: {}, eventType: '{}', Avro Payment ID: {}",
                env.getProperty("${policy.kafka.topics.payment-events}"), key, offset, eventType, event.getPolicyId());

        if (!"PAYMENT_PROCESSED".equals(eventType)) {
            log.warn("Discarding message from topic '{}' with non-matching eventType '{}'. Expected: 'PAYMENT_PROCESSED'. Key: {}",
                    env.getProperty("${policy.kafka.topics.payment-events}"), eventType, key);
            acknowledgment.acknowledge();
            return;
        }

        paymentProcessedService.processPayment(event)
                .doOnSuccess(policyRequest -> {
                    log.info("Payment processing completed for policyId: {}. New status: {}", policyRequest.getId(), policyRequest.getStatus());
                    acknowledgment.acknowledge();
                })
                .doOnError(e -> log.error("Error processing payment for policyId {} [key: {}, offset: {}]: {}", event.getPolicyId(), key, offset, e.getMessage()))
                .subscribe();
    }
}