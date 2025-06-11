package com.acmeinsurance.order.domain.strategy.fraudStrategy;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.enums.ClassificationEnum;
import com.acmeinsurance.order.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FraudRiskStrategyContext {

    private final Map<ClassificationEnum, FraudRiskStrategy> strategies;

    public FraudRiskStrategyContext(final List<FraudRiskStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(FraudRiskStrategy::getStrategyClassification, Function.identity()));
    }

    public Mono<PolicyRequest> evaluate(final FraudAnalysisResult fraudResult, final PolicyRequest policy) {
        return Optional.ofNullable(fraudResult)
                .map(FraudAnalysisResult::getClassification)
                .flatMap(classification -> Optional.ofNullable(strategies.get(classification)))
                .map(strategy -> strategy.evaluate(fraudResult, policy))
                .orElseThrow(() -> new IllegalArgumentException("No strategy found for FraudClassification: " +
                        (fraudResult != null ? fraudResult.getClassification() : "null fraudResult")));
    }
}