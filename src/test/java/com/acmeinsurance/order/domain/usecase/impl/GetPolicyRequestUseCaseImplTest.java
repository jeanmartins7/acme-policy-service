package com.acmeinsurance.order.domain.usecase.impl;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.handler.PolicyRequestNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class GetPolicyRequestUseCaseImplTest {

    private PolicyRequestRepository repository;
    private GetPolicyRequestUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        repository = mock(PolicyRequestRepository.class);
        useCase = new GetPolicyRequestUseCaseImpl(repository);
    }

    @Test
    void executeById_shouldReturnPolicyRequest_whenFound() {
        String id = "id1";
        PolicyRequest policyRequest = new PolicyRequest();
        when(repository.findById(id)).thenReturn(Mono.just(policyRequest));

        StepVerifier.create(useCase.executeById(id))
                .expectNext(policyRequest)
                .verifyComplete();

        verify(repository, times(1)).findById(id);
    }

    @Test
    void executeById_shouldReturnError_whenNotFound() {
        String id = "id2";
        when(repository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.executeById(id))
                .expectError(PolicyRequestNotFoundException.class)
                .verify();

        verify(repository, times(1)).findById(id);
    }

    @Test
    void executeByCustomerId_shouldReturnFluxOfPolicyRequests() {
        String customerId = "customer1";
        PolicyRequest pr1 = new PolicyRequest();
        PolicyRequest pr2 = new PolicyRequest();
        when(repository.findByCustomerId(customerId)).thenReturn(Flux.just(pr1, pr2));

        StepVerifier.create(useCase.executeByCustomerId(customerId))
                .expectNext(pr1)
                .expectNext(pr2)
                .verifyComplete();

        verify(repository, times(1)).findByCustomerId(customerId);
    }
}