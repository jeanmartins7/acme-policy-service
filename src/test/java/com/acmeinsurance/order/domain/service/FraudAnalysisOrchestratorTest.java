//package com.acmeinsurance.order.domain.service;
//
//import com.acmeinsurance.order.domain.model.PolicyRequest;
//import com.acmeinsurance.order.domain.strategy.fraudStrategy.FraudRiskStrategyContext;
//import com.acmeinsurance.order.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;
//import com.acmeinsurance.order.infrastructure.integration.fraud.service.FraudApiService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.util.UUID;
//
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//
//@ExtendWith(SpringExtension.class)
//class FraudAnalysisOrchestratorTest {
//
//
//    @Mock
//    private FraudApiService fraudApiService;
//
//    @Mock
//    private FraudRiskStrategyContext fraudRiskStrategyContext;
//
//    @InjectMocks
//    private FraudAnalysisOrchestrator orchestrator;
//
//
//    @Test
//    void analyze_shouldReturnPolicyRequest_whenNoFraudDetected() {
//        PolicyRequest policyRequest = mock(PolicyRequest.class);
//        FraudAnalysisResult fraudAnalysisResult = mock(FraudAnalysisResult.class);
//
//        when(policyRequest.getId()).thenReturn(UUID.randomUUID());
//        when(policyRequest.getCustomerId()).thenReturn(UUID.randomUUID());
//        when(fraudApiService.analyzePolicyRequest(any(UUID.class), any(UUID.class)))
//                .thenReturn(Mono.just(fraudAnalysisResult));
//        when(fraudRiskStrategyContext.evaluate(any(), any()))
//                .thenReturn(Mono.just(policyRequest));
//
//        when(fraudApiService.analyzePolicyRequest(UUID.randomUUID(), UUID.randomUUID())).thenReturn(Mono.just(mock(FraudAnalysisResult.class)));
//
//        StepVerifier.create(orchestrator.performAnalysisAndSave(policyRequest.getId().toString()))
//                .expectNext(policyRequest)
//                .verifyComplete();
//
//        verify(fraudApiService).analyzePolicyRequest(any(UUID.class), any(UUID.class));
//    }
//
//    @Test
//    void analyze_shouldReturnError_whenFraudDetected() {
//        PolicyRequest policyRequest = mock(PolicyRequest.class);
//
//        when(fraudApiService.analyzePolicyRequest(any(UUID.class), any(UUID.class)))
//                .thenReturn(Mono.error(new RuntimeException("Fraud detected")));
//
//        StepVerifier.create(orchestrator.performAnalysisAndSave(policyRequest.getId().toString()))
//                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("Fraud detected"))
//                .verify();
//
//        verify(fraudApiService).analyzePolicyRequest(any(UUID.class), any(UUID.class));
//    }
//}