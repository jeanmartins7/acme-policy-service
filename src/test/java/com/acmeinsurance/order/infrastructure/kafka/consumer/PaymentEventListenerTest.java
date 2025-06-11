package com.acmeinsurance.order.infrastructure.kafka.consumer;

import com.acmeinsurance.order.avro.PolicyStatusChangedEvent;
import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.service.PolicyStatusNotifier;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import com.acmeinsurance.order.infrastructure.kafka.producer.PolicyEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyStatusNotifierTest {

    private PolicyEventPublisher publisher;
    private PolicyStatusNotifier notifier;

    @BeforeEach
    void setUp() {
        publisher = mock(PolicyEventPublisher.class);
        notifier = new PolicyStatusNotifier(publisher);
    }

    @Test
    void notifyPolicyStatusChanged_shouldPublishEvent() {
        PolicyRequest policyRequest = mock(PolicyRequest.class);
        when(policyRequest.getId()).thenReturn(UUID.randomUUID());
        when(policyRequest.getCustomerId()).thenReturn(UUID.randomUUID());

        when(publisher.publishEventNotificationStatus(any(PolicyStatusChangedEvent.class), eq("NOTIFICATION_STATUS")))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                notifier.notifyPolicyStatusChanged(policyRequest, PolicyStatusEnum.PENDING, PolicyStatusEnum.APPROVED)
        ).verifyComplete();

        verify(publisher).publishEventNotificationStatus(any(PolicyStatusChangedEvent.class), eq("NOTIFICATION_STATUS"));
    }
}