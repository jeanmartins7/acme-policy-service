package com.acmeinsurance.policy.domain.strategy;

import com.acmeinsurance.policy.enums.ClassificationEnum;
import com.acmeinsurance.policy.enums.PolicyStatusEnum;
import com.acmeinsurance.policy.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FraudRiskStrategyContext {

    private final Map<ClassificationEnum, FraudRiskStrategy> strategies;

    public FraudRiskStrategyContext(List<FraudRiskStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(FraudRiskStrategy::getStrategyClassification, Function.identity()));
    }

    public PolicyStatusEnum evaluate(FraudAnalysisResult fraudResult) {
        FraudRiskStrategy strategy = strategies.get(fraudResult.getClassification());
        if (Objects.isNull(strategy)) {
            throw new IllegalArgumentException("No strategy found for FraudClassification: " + fraudResult.getClassification());
        }
        return strategy.evaluate(fraudResult);
    }
}