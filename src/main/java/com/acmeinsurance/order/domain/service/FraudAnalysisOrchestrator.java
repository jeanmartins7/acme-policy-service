package com.acmeinsurance.order.domain.service;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.domain.strategy.fraudStrategy.FraudRiskStrategyContext;
import com.acmeinsurance.order.domain.strategy.policyStatusStrategy.PolicyStatusValidationChain;
import com.acmeinsurance.order.infrastructure.integration.fraud.service.FraudApiService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FraudAnalysisOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(FraudAnalysisOrchestrator.class);
    private static final String POLICY_NOT_FOUND_DURING_CRITICAL_ERROR_RECOVERY = "Policy not found during critical error recovery.";
    private static final String POLICY_NOT_FOUND_DURING_FRAUD_API_ERROR_RECOVERY = "Policy not found during fraud API error recovery.";
    private static final String API_FRAUD_ANALYSIS_FAILED_FOR_POLICY_ID_MARKING_AS_REJECTED_FOR_SAFETY = "API fraud analysis failed for policyId {}. Marking as REJECTED for safety.";
    private static final String CRITICAL_UNHANDLED_ERROR_IN_ORCHESTRATION_FOR_POLICY_ID_MARKING_AS_REJECTED_FOR_SAFETY = "Critical unhandled error in orchestration for policyId {}. Marking as REJECTED for safety.";
    private static final String FRAUD_ANALYSIS_AND_STATUS_UPDATE_COMPLETED_FOR_POLICY_ID_FINAL_STATUS = "Fraud analysis and status update completed for policyId: {}. Final status: {}";

    private final PolicyRequestRepository policyRequestRepository;
    private final FraudApiService fraudApiService;
    private final FraudRiskStrategyContext fraudRiskStrategyContext;
    private final PolicyStatusValidationChain policyStatusValidationChain;

    public Mono<PolicyRequest> performAnalysisAndSave(final String policyId) {
        return policyRequestRepository.findById(policyId)
                .flatMap(this::analyzeAndEvaluateFraud)
                .doOnSuccess(policyRequest -> log.info(FRAUD_ANALYSIS_AND_STATUS_UPDATE_COMPLETED_FOR_POLICY_ID_FINAL_STATUS,
                        policyRequest.getId(), policyRequest.getStatus()))
                .doOnError(e -> log.error("Unhandled error in performAnalysisAndSave for policyId {}: {}", policyId, e.getMessage()))
                .onErrorResume(e -> handleError(policyId, e,
                        CRITICAL_UNHANDLED_ERROR_IN_ORCHESTRATION_FOR_POLICY_ID_MARKING_AS_REJECTED_FOR_SAFETY,
                        POLICY_NOT_FOUND_DURING_CRITICAL_ERROR_RECOVERY));
    }

    private Mono<PolicyRequest> analyzeAndEvaluateFraud(final PolicyRequest policyRequest) {
        return fraudApiService.analyzePolicyRequest(policyRequest.getId(), policyRequest.getCustomerId())
                .flatMap(fraudResult -> fraudRiskStrategyContext.evaluate(fraudResult, policyRequest)
                        .doOnSuccess(policyStatusValidationChain::applyPending))
                .doOnError(e -> log.error("Error during API fraud analysis for policyId {}: {}", policyRequest.getId(), e.getMessage()))
                .onErrorResume(e -> handleError(policyRequest.getId().toString(), e,
                        API_FRAUD_ANALYSIS_FAILED_FOR_POLICY_ID_MARKING_AS_REJECTED_FOR_SAFETY,
                        POLICY_NOT_FOUND_DURING_FRAUD_API_ERROR_RECOVERY));
    }


    private Mono<PolicyRequest> handleError(final String policyId, final Throwable e,
                                            final String logError, final String message) {
        log.error(logError, policyId, e);

        return policyRequestRepository.findById(policyId)
                .flatMap(pr -> Mono.just(pr)
                        .switchIfEmpty(Mono.error(new RuntimeException(message, e))));
    }
}