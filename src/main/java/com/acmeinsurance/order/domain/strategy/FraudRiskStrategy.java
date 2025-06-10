package com.acmeinsurance.order.domain.strategy;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.enums.ClassificationEnum;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import com.acmeinsurance.order.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;

public interface FraudRiskStrategy {

    PolicyStatusEnum evaluate(final FraudAnalysisResult fraudResult, final PolicyRequest policyRequest);
    ClassificationEnum getStrategyClassification();
}