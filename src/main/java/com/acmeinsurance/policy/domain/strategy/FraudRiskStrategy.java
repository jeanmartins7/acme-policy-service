package com.acmeinsurance.policy.domain.strategy;

import com.acmeinsurance.policy.enums.ClassificationEnum;
import com.acmeinsurance.policy.enums.PolicyStatusEnum;
import com.acmeinsurance.policy.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;

public interface FraudRiskStrategy {

    PolicyStatusEnum evaluate(final FraudAnalysisResult fraudResult);
    ClassificationEnum getStrategyClassification();
}