package com.acmeinsurance.policy.domain.strategy.impl;

import com.acmeinsurance.policy.domain.strategy.FraudRiskStrategy;
import com.acmeinsurance.policy.enums.ClassificationEnum;
import com.acmeinsurance.policy.enums.PolicyStatusEnum;
import com.acmeinsurance.policy.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;
import org.springframework.stereotype.Component;

@Component
public class HighRiskStrategy implements FraudRiskStrategy {
    @Override
    public PolicyStatusEnum evaluate(final FraudAnalysisResult fraudResult) {
        return PolicyStatusEnum.REJECTED;
    }

    @Override
    public ClassificationEnum getStrategyClassification() {
        return ClassificationEnum.HIGH_RISK;
    }
}