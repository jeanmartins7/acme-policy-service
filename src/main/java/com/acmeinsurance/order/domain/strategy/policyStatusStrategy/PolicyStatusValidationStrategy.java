package com.acmeinsurance.order.domain.strategy.policyStatusStrategy;

import com.acmeinsurance.order.avro.PaymentProcessedEvent;
import com.acmeinsurance.order.avro.SubscriptionProcessedEvent;
import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.enums.PolicyStatusEnum; // Importe o enum
import reactor.core.publisher.Mono;

public interface PolicyStatusValidationStrategy {

    PolicyStatusEnum getSupportedStatus();

    Mono<PolicyRequest> validateCancel(final PolicyRequest policyRequest);

    Mono<PolicyRequest> applyPending(final PolicyRequest policyRequest);

    Mono<PolicyRequest> applyPayment(final PolicyRequest policyRequest, final PaymentProcessedEvent paymentProcessedEvent);

    Mono<PolicyRequest> applySubscriptionAuthorized(final PolicyRequest policyRequest, final SubscriptionProcessedEvent subscriptionProcessedEvent);
}