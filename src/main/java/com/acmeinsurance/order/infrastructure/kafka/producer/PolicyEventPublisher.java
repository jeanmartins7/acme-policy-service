package com.acmeinsurance.order.infrastructure.kafka.producer;

import com.acmeinsurance.order.avro.PolicyReceivedEvent;
import com.acmeinsurance.order.avro.PolicyStatusChangedEvent;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class PolicyEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PolicyEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${policy.kafka.topics.status-notifications}")
    private String statusNotificationsTopic;

    @Value("${app.event.source-id}")
    private String eventSourceId;

    public Mono<Void> publishPolicyReceivedEvent(final PolicyReceivedEvent event, final String eventTypeHeader) {
        return buildAndSend(event, PolicyStatusEnum.RECEIVED.getValue(), eventTypeHeader, event.getPolicyId());
    }

    public Mono<Void> publishEventNotificationStatus(final PolicyStatusChangedEvent event, final String eventTypeHeader) {
        return buildAndSend(event, event.getNewStatus(), eventTypeHeader, event.getPolicyId());
    }

    private Mono<Void> buildAndSend(final Object eventPayload, final String key, final String eventTypeHeader, final CharSequence policyIdForLog) {
        return Mono.fromCallable(() -> {
                    Message<?> message = MessageBuilder.withPayload(eventPayload)
                            .setHeader(KafkaHeaders.KEY, key)
                            .setHeader(KafkaHeaders.TOPIC, statusNotificationsTopic)
                            .setHeader("eventType", eventTypeHeader)
                            .setHeader("__TypeId__", eventPayload.getClass().getName())
                            .setHeader("eventSourceId", eventSourceId)
                            .build();

                    kafkaTemplate.send(message);
                    log.info("Published event for policyId {} to topic {} with eventType header '{}' and eventSourceId '{}'",
                            policyIdForLog, statusNotificationsTopic, eventTypeHeader, eventSourceId);
                    return null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}