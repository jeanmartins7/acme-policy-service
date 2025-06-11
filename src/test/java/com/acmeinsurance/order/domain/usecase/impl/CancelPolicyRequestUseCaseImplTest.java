package com.acmeinsurance.order.domain.usecase.impl;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.domain.strategy.policyStatusStrategy.PolicyStatusValidationChain;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class CancelPolicyRequestUseCaseImplTest {

    private PolicyRequestRepository policyRequestRepository;
    private PolicyStatusValidationChain validationChain;
    private CancelPolicyRequestUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        policyRequestRepository = mock(PolicyRequestRepository.class);
        validationChain = mock(PolicyStatusValidationChain.class);
        useCase = new CancelPolicyRequestUseCaseImpl(policyRequestRepository, validationChain);
    }

    @Test
    void executeById_shouldCancelPolicyRequestSuccessfully() {
        String policyId = "policy123";
        PolicyRequest policyRequest = mock(PolicyRequest.class);
        PolicyRequest validatedPolicyRequest = mock(PolicyRequest.class);
        PolicyRequest cancelledPolicyRequest = mock(PolicyRequest.class);

        when(policyRequestRepository.findById(policyId)).thenReturn(Mono.just(policyRequest));
        when(validationChain.validateCancel(policyRequest)).thenReturn(Mono.just(validatedPolicyRequest));
        when(validatedPolicyRequest.getStatus()).thenReturn(PolicyStatusEnum.VALIDATED);
        when(policyRequestRepository.updateStatus(validatedPolicyRequest, PolicyStatusEnum.VALIDATED, PolicyStatusEnum.CANCELLED))
                .thenReturn(Mono.just(cancelledPolicyRequest));

        StepVerifier.create(useCase.executeById(policyId))
                .expectNext(cancelledPolicyRequest)
                .verifyComplete();

        verify(policyRequestRepository).findById(policyId);
        verify(validationChain).validateCancel(policyRequest);
        verify(policyRequestRepository).updateStatus(validatedPolicyRequest, PolicyStatusEnum.VALIDATED, PolicyStatusEnum.CANCELLED);
    }

    @Test
    void executeById_shouldReturnEmptyWhenPolicyNotFound() {
        String policyId = "notfound";
        when(policyRequestRepository.findById(policyId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.executeById(policyId))
                .verifyComplete();

        verify(policyRequestRepository).findById(policyId);
        verifyNoMoreInteractions(validationChain, policyRequestRepository);
    }

    @Test
    void executeById_shouldPropagateErrorFromValidationChain() {
        String policyId = "policyError";
        PolicyRequest policyRequest = mock(PolicyRequest.class);
        RuntimeException error = new RuntimeException("Validation failed");

        when(policyRequestRepository.findById(policyId)).thenReturn(Mono.just(policyRequest));
        when(validationChain.validateCancel(policyRequest)).thenReturn(Mono.error(error));

        StepVerifier.create(useCase.executeById(policyId))
                .expectErrorMatches(e -> e == error)
                .verify();

        verify(policyRequestRepository).findById(policyId);
        verify(validationChain).validateCancel(policyRequest);
    }
}