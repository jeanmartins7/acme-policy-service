package com.acmeinsurance.policy.domain.strategy.impl;

import com.acmeinsurance.policy.domain.strategy.FraudRiskStrategy;
import com.acmeinsurance.policy.enums.ClassificationEnum;
import com.acmeinsurance.policy.enums.PolicyStatusEnum;
import com.acmeinsurance.policy.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;
import org.springframework.stereotype.Component;

@Component
public class NoInformationRiskStrategy implements FraudRiskStrategy {
    @Override
    public PolicyStatusEnum evaluate(final FraudAnalysisResult fraudResult) {
        // Lógica mais complexa aqui, por exemplo, verificar o valor do seguro
        // Por enquanto, validar sempre
        return PolicyStatusEnum.VALIDATED;
    }

    @Override
    public ClassificationEnum getStrategyClassification() {
        return ClassificationEnum.SEM_INFORMACAO;
    }
}