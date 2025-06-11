package com.acmeinsurance.order.domain.strategy.policyStatusStrategy.impl;

import com.acmeinsurance.order.avro.PaymentProcessedEvent;
import com.acmeinsurance.order.avro.SubscriptionProcessedEvent;
import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.domain.strategy.policyStatusStrategy.PolicyStatusValidationStrategy;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ValidatedValidationStrategy implements PolicyStatusValidationStrategy {

    private final PolicyRequestRepository policyRequestRepository;
    private static final Logger log = LoggerFactory.getLogger(ValidatedValidationStrategy.class);


    @Override
    public PolicyStatusEnum getSupportedStatus() {
        return PolicyStatusEnum.VALIDATED;
    }

    @Override
    public Mono<PolicyRequest> validateCancel(final PolicyRequest policyRequest) {

        return Mono.just(policyRequest);
    }

    @Override
    public Mono<PolicyRequest> applyPending(final PolicyRequest policyRequest) {

        log.info("PolicyRequest {} status is VALIDATED by fraud strategy, but it will be set to PENDING for subsequent steps.", policyRequest.getId());

        return policyRequestRepository.updateStatus(policyRequest, policyRequest.getStatus(), PolicyStatusEnum.PENDING);
    }

    @Override
    public Mono<PolicyRequest> applyPayment(final PolicyRequest policyRequest, final PaymentProcessedEvent paymentProcessedEvent) {
        return Mono.just(policyRequest);
    }

    @Override
    public Mono<PolicyRequest> applySubscriptionAuthorized(final PolicyRequest policyRequest, final SubscriptionProcessedEvent subscriptionProcessedEvent) {
        return Mono.just(policyRequest);
    }
}