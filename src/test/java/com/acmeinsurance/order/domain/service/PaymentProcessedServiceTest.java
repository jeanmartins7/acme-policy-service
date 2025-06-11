//package com.acmeinsurance.order.domain.service;
//
//import com.acmeinsurance.order.avro.PaymentProcessedEvent;
//import com.acmeinsurance.order.domain.model.PolicyRequest;
//import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
//import com.acmeinsurance.order.domain.strategy.policyStatusStrategy.PolicyStatusValidationChain;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.util.UUID;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//class PaymentProcessedServiceTest {
//
//    @Mock
//    private PolicyRequestRepository policyRequestRepository;
//    @Mock
//    private PolicyStatusValidationChain policyStatusValidationChain;
//    @InjectMocks
//    private PaymentProcessedService service;
//
//    @Test
//    void processPayment_shouldApplyStatusStrategy_whenPolicyFound() {
//        PaymentProcessedEvent event = mock(PaymentProcessedEvent.class);
//        PolicyRequest policyRequest = mock(PolicyRequest.class);
//        PolicyRequest updatedPolicyRequest = mock(PolicyRequest.class);
//        final String uuid = UUID.randomUUID().toString();
//        when(event.getPolicyId()).thenReturn(uuid);
//        when(policyRequestRepository.findById(uuid)).thenReturn(Mono.just(policyRequest));
//
//        StepVerifier.create(service.processPayment(event))
//                .expectNext(updatedPolicyRequest)
//                .verifyComplete();
//
//        verify(policyRequestRepository).findById(uuid);
//    }
//
//    @Test
//    void processPayment_shouldComplete_whenPolicyNotFound() {
//        PaymentProcessedEvent event = mock(PaymentProcessedEvent.class);
//        when(event.getPolicyId()).thenReturn(UUID.randomUUID().toString());
//        when(policyRequestRepository.findById(UUID.randomUUID().toString())).thenReturn(Mono.empty());
//        final String uuid = UUID.randomUUID().toString();
//
//        StepVerifier.create(service.processPayment(event))
//                .verifyComplete();
//
//        verify(policyRequestRepository).findById(UUID.randomUUID().toString());
//    }
//
//    @Test
//    void processPayment_shouldReturnError_whenStatusStrategyFails() {
//        PaymentProcessedEvent event = mock(PaymentProcessedEvent.class);
//        PolicyRequest policyRequest = mock(PolicyRequest.class);
//        final String uuid = UUID.randomUUID().toString();
//
//        when(event.getPolicyId()).thenReturn(uuid);
//        when(policyRequestRepository.findById(uuid)).thenReturn(Mono.just(policyRequest));
//
//        StepVerifier.create(service.processPayment(event))
//                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("Strategy error"))
//                .verify();
//
//        verify(policyRequestRepository).findById(uuid);
//    }
//}