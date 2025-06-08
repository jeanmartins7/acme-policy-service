package com.acmeinsurance.policy.domain.usecase;


import com.acmeinsurance.policy.domain.model.PolicyRequest;
import com.acmeinsurance.policy.domain.service.FraudAnalysisOrchestrator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProcessFraudAnalysisUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessFraudAnalysisUseCase.class);

    private final FraudAnalysisOrchestrator fraudAnalysisOrchestrator;

    public Mono<PolicyRequest> execute(final String policyId) {

        log.info("Executing fraud analysis use case for policyId: {}", policyId);
        return fraudAnalysisOrchestrator.performAnalysisAndSave(policyId);
    }

}