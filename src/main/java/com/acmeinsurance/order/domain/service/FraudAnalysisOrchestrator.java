package com.acmeinsurance.order.domain.service;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.domain.strategy.FraudRiskStrategyContext;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import com.acmeinsurance.order.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;
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

    private final PolicyRequestRepository policyRequestRepository;
    private final FraudApiService fraudApiService;
    private final FraudRiskStrategyContext fraudRiskStrategyContext;

    public Mono<PolicyRequest> performAnalysisAndSave(final String policyId) {
        return policyRequestRepository.findById(policyId)
                .flatMap(policyRequest ->
                        fraudApiService.analyzePolicyRequest(policyRequest.getId(), policyRequest.getCustomerId())
                                .flatMap(fraudResult ->
                                        fraudAnalysis(policyId, policyRequest, fraudResult)
                                                .doOnSuccess(policyRisk -> policyIsValid(policyRequest, policyRisk))
                                                .doOnError(e -> log.error("Error during fraud analysis update for policyId {}: {}", policyId, e.getMessage()))
                                )
                                .doOnError(e -> log.error("Error during fraud analysis for policyId {}: {}", policyId, e.getMessage()))
                                .onErrorResume(e -> {
                                    log.error("Critical error in fraud analysis for policyId {}. Marking as REJECTED for safety.", policyId, e);
                                    return policyRequestRepository.findById(policyId)
                                            .flatMap(pr -> policyRequestRepository.updateStatus(pr, pr.getStatus(), PolicyStatusEnum.REJECTED))
                                            .switchIfEmpty(Mono.error(new RuntimeException("Policy not found during critical error recovery.", e)));
                                })
                );
    }

    private void policyIsValid(final PolicyRequest policyRequest, final PolicyRequest policyRisk) {
        if (PolicyStatusEnum.VALIDATED.equals(policyRisk.getStatus())) {

            log.info("PolicyRequest {} status updated to {} based on fraud analysis.", policyRequest.getId(), PolicyStatusEnum.PENDING);
            policyRequestRepository.updateStatus(policyRequest, policyRequest.getStatus(), PolicyStatusEnum.PENDING).subscribe();
        }
    }

    private Mono<PolicyRequest> fraudAnalysis(final String policyId, final PolicyRequest policyRequest, final FraudAnalysisResult fraudResult) {

        log.info("Starting fraud analysis orchestration for policyId: {}", policyId);
        final PolicyStatusEnum newStatus = fraudRiskStrategyContext.evaluate(fraudResult, policyRequest);

        log.info("PolicyRequest {} status updated to {} based on fraud analysis.", policyRequest.getId(), newStatus);
        return policyRequestRepository.updateStatus(policyRequest, policyRequest.getStatus(), newStatus);
    }
}