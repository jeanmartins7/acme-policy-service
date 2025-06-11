package com.acmeinsurance.order.domain.service;

import com.acmeinsurance.order.avro.SubscriptionProcessedEvent;
import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.domain.strategy.policyStatusStrategy.PolicyStatusValidationChain;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static com.acmeinsurance.order.enums.StatusEnum.CONFIRMED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SubscriptionProcessedServiceTest {

    private PolicyRequestRepository policyRequestRepository;
    private PolicyStatusValidationChain policyStatusValidationChain;
    private SubscriptionProcessedService service;

    @BeforeEach
    void setUp() {
        policyRequestRepository = mock(PolicyRequestRepository.class);
        policyStatusValidationChain = mock(PolicyStatusValidationChain.class);
        service = new SubscriptionProcessedService(policyRequestRepository, policyStatusValidationChain);
    }

    @Test
    void processSubscription_shouldApplyStatusStrategy_whenPolicyFound() {
        SubscriptionProcessedEvent event = mock(SubscriptionProcessedEvent.class);
        PolicyRequest policyRequest = mock(PolicyRequest.class);
        PolicyRequest updatedPolicyRequest = mock(PolicyRequest.class);

        when(event.getPolicyId()).thenReturn("policyId");
        when(policyRequestRepository.findById("policyId")).thenReturn(Mono.just(policyRequest));
        when(policyRequest.getId()).thenReturn(UUID.randomUUID());
        when(policyRequest.getStatus()).thenReturn(PolicyStatusEnum.VALIDATED);
        when(policyRequest.getPaymentConfirmed()).thenReturn(CONFIRMED.getValue());
        when(event.getStatus()).thenReturn("SUCCESS");
        when(policyStatusValidationChain.applySubscriptionAuthorized(policyRequest, event))
                .thenReturn(Mono.just(updatedPolicyRequest));

        StepVerifier.create(service.processSubscription(event))
                .expectNext(updatedPolicyRequest)
                .verifyComplete();

        verify(policyRequestRepository).findById("policyId");
        verify(policyStatusValidationChain).applySubscriptionAuthorized(policyRequest, event);
    }

    @Test
    void processSubscription_shouldReturnError_whenPolicyNotFound() {
        SubscriptionProcessedEvent event = mock(SubscriptionProcessedEvent.class);
        when(event.getPolicyId()).thenReturn("notfound");
        when(policyRequestRepository.findById("notfound")).thenReturn(Mono.empty());

        StepVerifier.create(service.processSubscription(event))
                .verifyComplete();

        verify(policyRequestRepository).findById("notfound");
        verifyNoInteractions(policyStatusValidationChain);
    }

    @Test
    void processSubscription_shouldReturnError_whenStatusStrategyFails() {
        SubscriptionProcessedEvent event = mock(SubscriptionProcessedEvent.class);
        PolicyRequest policyRequest = mock(PolicyRequest.class);

        when(event.getPolicyId()).thenReturn("policyId");
        when(policyRequestRepository.findById("policyId")).thenReturn(Mono.just(policyRequest));
        when(policyRequest.getId()).thenReturn(UUID.randomUUID());
        when(policyRequest.getStatus()).thenReturn(PolicyStatusEnum.VALIDATED);
        when(policyRequest.getPaymentConfirmed()).thenReturn(CONFIRMED.getValue());
        when(event.getStatus()).thenReturn("FAILURE");
        when(policyStatusValidationChain.applySubscriptionAuthorized(policyRequest, event))
                .thenReturn(Mono.error(new RuntimeException("Strategy error")));

        StepVerifier.create(service.processSubscription(event))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("Strategy error"))
                .verify();

        verify(policyRequestRepository).findById("policyId");
        verify(policyStatusValidationChain).applySubscriptionAuthorized(policyRequest, event);
    }
}