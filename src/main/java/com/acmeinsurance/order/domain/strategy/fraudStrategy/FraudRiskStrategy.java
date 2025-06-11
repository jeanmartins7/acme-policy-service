package com.acmeinsurance.order.domain.strategy.fraudStrategy;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.enums.ClassificationEnum;
import com.acmeinsurance.order.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;
import reactor.core.publisher.Mono;

public interface FraudRiskStrategy {

    Mono<PolicyRequest> evaluate(final FraudAnalysisResult fraudResult, final PolicyRequest policyRequest);

    ClassificationEnum getStrategyClassification();
}