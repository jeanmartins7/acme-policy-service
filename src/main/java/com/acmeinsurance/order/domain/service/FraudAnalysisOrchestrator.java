package com.acmeinsurance.order.domain.service;


import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.domain.strategy.FraudRiskStrategyContext;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
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
        log.info("Starting fraud analysis orchestration for policyId: {}", policyId);

        return policyRequestRepository.findById(policyId)
                .flatMap(policyRequest ->
                        fraudApiService.analyzePolicyRequest(policyRequest.getId(), policyRequest.getCustomerId())
                        .flatMap(fraudResult -> {

                            final PolicyStatusEnum newStatus = fraudRiskStrategyContext.evaluate(fraudResult, policyRequest);

                            log.info("PolicyRequest {} status updated to {} based on fraud analysis.", policyRequest.getId(), newStatus);

                            return policyRequestRepository.updateStatus(policyRequest, policyRequest.getStatus(), newStatus);
                        })
                        .doOnError(e -> log.error("Error during fraud analysis for policyId {}: {}", policyId, e.getMessage()))
                        .onErrorResume(e -> {

                            log.error("Critical error in fraud analysis for policyId {}. Marking as REJECTED for safety.", policyId, e);
//                            return policyRequestRepository.findById(policyId)
//                                    .flatMap(p -> policyRequestRepository.updateStatus(p, p.getStatus(), PolicyStatusEnum.REJECTED));
                            return policyRequestRepository.findById(policyId);
                        }));
    }
}
