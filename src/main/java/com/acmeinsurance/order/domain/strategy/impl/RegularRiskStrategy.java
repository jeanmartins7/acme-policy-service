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
public class RegularRiskStrategy implements FraudRiskStrategy {

    private static final Long QUINHENTOS_K = 500000L;
    private static final Long TREZENTOS_CINQUENTA_K = 350000L;
    private static final Long DUZENTOS_CINQUENTA_CINCO_K = 255000L;

    @FunctionalInterface
    private interface CategoryEvaluator {
        PolicyStatusEnum evaluate(PolicyRequest policy);
    }

    private final Map<CategoryEnum, CategoryEvaluator> evaluators;

    public RegularRiskStrategy() {
        evaluators = new EnumMap<>(CategoryEnum.class);

        evaluators.put(CategoryEnum.VIDA, policy ->
                BigDecimal.valueOf(QUINHENTOS_K).compareTo(policy.getInsuredAmount()) < 0 ?
                        PolicyStatusEnum.APPROVED : PolicyStatusEnum.REJECTED
        );
        evaluators.put(CategoryEnum.RESIDENCIAL, policy ->
                BigDecimal.valueOf(QUINHENTOS_K).compareTo(policy.getInsuredAmount()) < 0 ?
                        PolicyStatusEnum.APPROVED : PolicyStatusEnum.REJECTED
        );
        evaluators.put(CategoryEnum.AUTO, policy ->
                BigDecimal.valueOf(TREZENTOS_CINQUENTA_K).compareTo(policy.getInsuredAmount()) < 0 ?
                        PolicyStatusEnum.APPROVED : PolicyStatusEnum.REJECTED
        );
        evaluators.put(CategoryEnum.OUTRO, policy ->
                BigDecimal.valueOf(DUZENTOS_CINQUENTA_CINCO_K).compareTo(policy.getInsuredAmount()) < 0 ?
                        PolicyStatusEnum.APPROVED : PolicyStatusEnum.REJECTED
        );

    }

    @Override
    public PolicyStatusEnum evaluate(final FraudAnalysisResult fraudResult, final PolicyRequest policy) {

        final CategoryEvaluator evaluator = evaluators.get(policy.getCategory());

        if (evaluator != null) {
            return evaluator.evaluate(policy);
        } else {
            System.err.println("No specific evaluation logic found for category: " + policy.getCategory());
            return PolicyStatusEnum.REJECTED;
        }
    }

    @Override
    public ClassificationEnum getStrategyClassification() {
        return ClassificationEnum.REGULAR;
    }
}