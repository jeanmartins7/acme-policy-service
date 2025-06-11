package com.acmeinsurance.order.domain.usecase;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.service.FraudAnalysisOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessFraudAnalysisUseCaseTest {

    private FraudAnalysisOrchestrator fraudAnalysisOrchestrator;
    private ProcessFraudAnalysisUseCase useCase;

    @BeforeEach
    void setUp() {
        fraudAnalysisOrchestrator = mock(FraudAnalysisOrchestrator.class);
        useCase = new ProcessFraudAnalysisUseCase(fraudAnalysisOrchestrator);
    }

    @Test
    void execute_shouldCallOrchestratorAndReturnResult() {
        String policyId = "123";
        PolicyRequest expected = new PolicyRequest();
        when(fraudAnalysisOrchestrator.performAnalysisAndSave(policyId))
                .thenReturn(Mono.just(expected));

        Mono<PolicyRequest> result = useCase.execute(policyId);

        StepVerifier.create(result)
                .expectNext(expected)
                .verifyComplete();

        verify(fraudAnalysisOrchestrator, times(1)).performAnalysisAndSave(policyId);
    }

    @Test
    void execute_shouldPropagateErrorFromOrchestrator() {
        String policyId = "456";
        RuntimeException error = new RuntimeException("Fraud error");
        when(fraudAnalysisOrchestrator.performAnalysisAndSave(policyId))
                .thenReturn(Mono.error(error));

        Mono<PolicyRequest> result = useCase.execute(policyId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable == error)
                .verify();

        verify(fraudAnalysisOrchestrator, times(1)).performAnalysisAndSave(policyId);
    }
}