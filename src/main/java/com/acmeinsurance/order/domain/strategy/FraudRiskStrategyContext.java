package com.acmeinsurance.order.domain.strategy;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.enums.ClassificationEnum;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import com.acmeinsurance.order.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FraudRiskStrategyContext {

    private final Map<ClassificationEnum, FraudRiskStrategy> strategies;

    public FraudRiskStrategyContext(final List<FraudRiskStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(FraudRiskStrategy::getStrategyClassification, Function.identity()));
    }

    public PolicyStatusEnum evaluate(final FraudAnalysisResult fraudResult, final PolicyRequest policy) {
        FraudRiskStrategy strategy = strategies.get(fraudResult.getClassification());
        if (Objects.isNull(strategy)) {
            throw new IllegalArgumentException("No strategy found for FraudClassification: " + fraudResult.getClassification());
        }
        return strategy.evaluate(fraudResult, policy);
    }
}