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
public class NoInformationRiskStrategy implements FraudRiskStrategy {

    private static final Long CINQUENTA_CINQUENTA_K = 55000L;
    private static final Long DUZENTOS_K = 200000L;
    private static final Long SETENTA_CINCO_K = 75000L;

    @FunctionalInterface
    private interface CategoryEvaluator {
        PolicyStatusEnum evaluate(PolicyRequest policy);
    }

    private final Map<CategoryEnum, NoInformationRiskStrategy.CategoryEvaluator> evaluators;

    public NoInformationRiskStrategy() {
        evaluators = new EnumMap<>(CategoryEnum.class);

        evaluators.put(CategoryEnum.VIDA, policy ->
                BigDecimal.valueOf(DUZENTOS_K).compareTo(policy.getInsuredAmount()) < 0 ?
                        PolicyStatusEnum.APPROVED : PolicyStatusEnum.REJECTED
        );
        evaluators.put(CategoryEnum.RESIDENCIAL, policy ->
                BigDecimal.valueOf(DUZENTOS_K).compareTo(policy.getInsuredAmount()) < 0 ?
                        PolicyStatusEnum.APPROVED : PolicyStatusEnum.REJECTED
        );
        evaluators.put(CategoryEnum.AUTO, policy ->
                BigDecimal.valueOf(SETENTA_CINCO_K).compareTo(policy.getInsuredAmount()) < 0 ?
                        PolicyStatusEnum.APPROVED : PolicyStatusEnum.REJECTED
        );
        evaluators.put(CategoryEnum.OUTRO, policy ->
                BigDecimal.valueOf(CINQUENTA_CINQUENTA_K).compareTo(policy.getInsuredAmount()) < 0 ?
                        PolicyStatusEnum.APPROVED : PolicyStatusEnum.REJECTED
        );

    }

    @Override
    public PolicyStatusEnum evaluate(final FraudAnalysisResult fraudResult, final PolicyRequest policy) {

        final NoInformationRiskStrategy.CategoryEvaluator evaluator = evaluators.get(policy.getCategory());

        if (evaluator != null) {
            return evaluator.evaluate(policy);
        } else {
            System.err.println("No specific evaluation logic found for category: " + policy.getCategory());
            return PolicyStatusEnum.REJECTED;
        }
    }

    @Override
    public ClassificationEnum getStrategyClassification() {
        return ClassificationEnum.SEM_INFORMACAO;
    }
}