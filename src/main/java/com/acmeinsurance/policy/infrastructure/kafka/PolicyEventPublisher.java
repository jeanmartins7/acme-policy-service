package com.acmeinsurance.policy.infrastructure.kafka;

import com.acmeinsurance.policy.avro.PolicyReceivedEvent;
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

    @Value("${app.event.source-id:default-source}")
    private String eventSourceId;

    public Mono<Void> publishPolicyReceivedEvent(final PolicyReceivedEvent event, final String eventTypeHeader) {
        return Mono.fromCallable(() -> {

                    final Message<PolicyReceivedEvent> message = MessageBuilder.withPayload(event)
                            .setHeader(KafkaHeaders.KEY, event.getPolicyId())
                            .setHeader(KafkaHeaders.TOPIC, statusNotificationsTopic)
                            .setHeader("eventType", eventTypeHeader)
                            .setHeader("eventSourceId", eventSourceId)
                            .build();

                    kafkaTemplate.send(message);
                    log.info("Published PolicyReceivedEvent for policyId {} to topic {} with eventType header '{}' and eventSourceId '{}'",
                            event.getPolicyId(), statusNotificationsTopic, eventTypeHeader, eventSourceId);
                    return null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}