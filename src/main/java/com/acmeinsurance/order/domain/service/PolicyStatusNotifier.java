package com.acmeinsurance.order.domain.service;

import com.acmeinsurance.order.avro.PolicyStatusChangedEvent;
import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import com.acmeinsurance.order.infrastructure.kafka.producer.PolicyEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PolicyStatusNotifier {

    private static final Logger log = LoggerFactory.getLogger(PolicyStatusNotifier.class);
    private static final String NOTIFICATION_STATUS = "NOTIFICATION_STATUS";

    private final PolicyEventPublisher policyEventPublisher;

    public Mono<Void> notifyPolicyStatusChanged(
            final PolicyRequest policyRequest,
            final PolicyStatusEnum oldStatus,
            final PolicyStatusEnum newStatus) {

        final PolicyStatusChangedEvent avroEvent = PolicyStatusChangedEvent.newBuilder()
                .setPolicyId(policyRequest.getId().toString())
                .setCustomerId(policyRequest.getCustomerId().toString())
                .setOldStatus(getOldStatus(oldStatus))
                .setNewStatus(newStatus.getValue())
                .setChangeTimestamp(LocalDateTime.now().toString())
                .build();

        log.info("Notifying status change for policyId {}: {} -> {}. ",
                policyRequest.getId(), oldStatus, newStatus);

        return policyEventPublisher.publishEventNotificationStatus(avroEvent, NOTIFICATION_STATUS);
    }

    private static String getOldStatus(final PolicyStatusEnum oldStatus) {
        return String.valueOf(Optional.of(oldStatus.getValue()).or(null));
    }
}