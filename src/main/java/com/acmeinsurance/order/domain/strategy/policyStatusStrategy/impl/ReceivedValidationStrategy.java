package com.acmeinsurance.order.domain.strategy.policyStatusStrategy.impl;

import com.acmeinsurance.order.avro.PaymentProcessedEvent;
import com.acmeinsurance.order.avro.SubscriptionProcessedEvent;
import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.strategy.policyStatusStrategy.PolicyStatusValidationStrategy;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReceivedValidationStrategy implements PolicyStatusValidationStrategy {

    @Override
    public PolicyStatusEnum getSupportedStatus() {
        return PolicyStatusEnum.RECEIVED;
    }

    @Override
    public Mono<PolicyRequest> validateCancel(final PolicyRequest policyRequest) {

        return Mono.just(policyRequest);
    }

    @Override
    public Mono<PolicyRequest> applyPending(final PolicyRequest policyRequest) {
        return Mono.just(policyRequest);
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