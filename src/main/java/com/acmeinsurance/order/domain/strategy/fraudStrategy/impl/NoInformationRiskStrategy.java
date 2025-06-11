package com.acmeinsurance.order.domain.strategy.fraudStrategy.impl;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.domain.strategy.fraudStrategy.FraudRiskStrategy;
import com.acmeinsurance.order.enums.CategoryEnum;
import com.acmeinsurance.order.enums.ClassificationEnum;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import com.acmeinsurance.order.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NoInformationRiskStrategy implements FraudRiskStrategy {

    private static final Logger log = LoggerFactory.getLogger(NoInformationRiskStrategy.class);
    private final PolicyRequestRepository policyRequestRepository;


    private static final BigDecimal DUZENTOS_K = BigDecimal.valueOf(200000L);
    private static final BigDecimal SETENTA_CINCO_K = BigDecimal.valueOf(75000L);
    private static final BigDecimal CINQUENTA_CINQUENTA_K = BigDecimal.valueOf(55000L);

    @FunctionalInterface
    private interface CategoryEvaluator {
        PolicyStatusEnum evaluate(PolicyRequest policy);
    }

    private final Map<CategoryEnum, NoInformationRiskStrategy.CategoryEvaluator> evaluators = new EnumMap<>(CategoryEnum.class);

    {
        evaluators.put(CategoryEnum.VIDA, policy ->
                DUZENTOS_K.compareTo(policy.getInsuredAmount()) > 0 ?
                        PolicyStatusEnum.VALIDATED : PolicyStatusEnum.REJECTED
        );
        evaluators.put(CategoryEnum.RESIDENCIAL, policy ->
                DUZENTOS_K.compareTo(policy.getInsuredAmount()) > 0 ?
                        PolicyStatusEnum.VALIDATED : PolicyStatusEnum.REJECTED
        );
        evaluators.put(CategoryEnum.AUTO, policy ->
                SETENTA_CINCO_K.compareTo(policy.getInsuredAmount()) > 0 ?
                        PolicyStatusEnum.VALIDATED : PolicyStatusEnum.REJECTED
        );
        evaluators.put(CategoryEnum.OUTRO, policy ->
                CINQUENTA_CINQUENTA_K.compareTo(policy.getInsuredAmount()) > 0 ?
                        PolicyStatusEnum.VALIDATED : PolicyStatusEnum.REJECTED
        );

    }

    @Override
    public Mono<PolicyRequest> evaluate(final FraudAnalysisResult fraudResult, final PolicyRequest policy) {

        log.info("PolicyRequest {} evaluated to status {} by HighRiskStrategy. Updating repository.", policy.getId(),
                evaluators.get(policy.getCategory()).evaluate(policy));

        return policyRequestRepository.updateStatus(policy,
                policy.getStatus(),
                evaluators.get(policy.getCategory()).evaluate(policy));
    }

    @Override
    public ClassificationEnum getStrategyClassification() {
        return ClassificationEnum.SEM_INFORMACAO;
    }
}