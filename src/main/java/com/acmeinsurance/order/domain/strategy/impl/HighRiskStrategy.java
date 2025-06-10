package com.acmeinsurance.order.domain.strategy.impl;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.strategy.FraudRiskStrategy;
import com.acmeinsurance.order.enums.CategoryEnum;
import com.acmeinsurance.order.enums.ClassificationEnum;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import com.acmeinsurance.order.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Component
public class HighRiskStrategy implements FraudRiskStrategy {

    private static final Long CEM_CINQUENTA_K = 150000L;
    private static final Long DUZENTOS_CINQUENTA_K = 250000L;
    private static final Long CEM_VINTE_CINCO_K = 125000L;

    @FunctionalInterface
    private interface CategoryEvaluator {
        PolicyStatusEnum evaluate(PolicyRequest policy);
    }

    private final Map<CategoryEnum, HighRiskStrategy.CategoryEvaluator> evaluators;

    public HighRiskStrategy() {
        evaluators = new EnumMap<>(CategoryEnum.class);

        evaluators.put(CategoryEnum.VIDA, policy ->
                BigDecimal.valueOf(DUZENTOS_CINQUENTA_K).compareTo(policy.getInsuredAmount()) < 0 ?
                        PolicyStatusEnum.VALIDATED : PolicyStatusEnum.REJECTED
        );
        evaluators.put(CategoryEnum.RESIDENCIAL, policy ->
                BigDecimal.valueOf(DUZENTOS_CINQUENTA_K).compareTo(policy.getInsuredAmount()) < 0 ?
                        PolicyStatusEnum.VALIDATED : PolicyStatusEnum.REJECTED
        );
        evaluators.put(CategoryEnum.AUTO, policy ->
                BigDecimal.valueOf(CEM_CINQUENTA_K).compareTo(policy.getInsuredAmount()) < 0 ?
                        PolicyStatusEnum.VALIDATED : PolicyStatusEnum.REJECTED
        );
        evaluators.put(CategoryEnum.OUTRO, policy ->
                BigDecimal.valueOf(CEM_VINTE_CINCO_K).compareTo(policy.getInsuredAmount()) < 0 ?
                        PolicyStatusEnum.VALIDATED : PolicyStatusEnum.REJECTED
        );

    }

    @Override
    public PolicyStatusEnum evaluate(final FraudAnalysisResult fraudResult, final PolicyRequest policy) {

        final HighRiskStrategy.CategoryEvaluator evaluator = evaluators.get(policy.getCategory());

        if (evaluator != null) {
            return evaluator.evaluate(policy);
        } else {
            System.err.println("No specific evaluation logic found for category: " + policy.getCategory());
            return PolicyStatusEnum.REJECTED;
        }
    }

    @Override
    public ClassificationEnum getStrategyClassification() {
        return ClassificationEnum.HIGH_RISK;
    }
}