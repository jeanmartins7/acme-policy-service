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

import static com.acmeinsurance.order.utils.ValidateUtils.isActived;
import static com.acmeinsurance.order.utils.ValidateUtils.isConfirmed;
import static com.acmeinsurance.order.utils.ValidateUtils.isDenied;
import static com.acmeinsurance.order.utils.ValidateUtils.isInactived;

@Component
@RequiredArgsConstructor
public class PendingValidationStrategy implements PolicyStatusValidationStrategy {

    private static final Logger log = LoggerFactory.getLogger(PendingValidationStrategy.class);


    private final PolicyRequestRepository policyRequestRepository;

    @Override
    public PolicyStatusEnum getSupportedStatus() {
        return PolicyStatusEnum.PENDING;
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

        if (isDenied(paymentProcessedEvent.getStatus())) {

            policyRequest.markPaymentConfirmed(false);

            return callUpdateStatus(policyRequest, policyRequest.getStatus(), PolicyStatusEnum.REJECTED);
        }

        if (isConfirmed(paymentProcessedEvent.getStatus())) {
            policyRequest.markPaymentConfirmed(true);
        }

        return isAuthorized(policyRequest);
    }


    @Override
    public Mono<PolicyRequest> applySubscriptionAuthorized(final PolicyRequest policyRequest, final SubscriptionProcessedEvent subscriptionProcessedEvent) {


        if (isInactived(subscriptionProcessedEvent.getStatus())) {

            policyRequest.markSubscriptionAuthorized(false);

            return callUpdateStatus(policyRequest, policyRequest.getStatus(), PolicyStatusEnum.REJECTED);
        }

        if (isActived(subscriptionProcessedEvent.getStatus())) {

            policyRequest.markSubscriptionAuthorized(true);

        }

        return isAuthorized(policyRequest);
    }

    private Mono<PolicyRequest> isAuthorized(final PolicyRequest policyRequest) {

        if (policyRequest.getStatus().equals(PolicyStatusEnum.PENDING) &&
                isConfirmed(policyRequest.getPaymentConfirmed()) &&
                isConfirmed(policyRequest.getSubscriptionAuthorized())) {

            log.info("Policy ID: {} has both Payment CONFIRMED and Subscription AUTHORIZED. Transitioning to APPROVED.", policyRequest.getId());
            return callUpdateStatus(policyRequest, policyRequest.getStatus(), PolicyStatusEnum.APPROVED);
        }

        return callUpdateStatus(policyRequest);
    }

    private Mono<PolicyRequest> callUpdateStatus(final PolicyRequest policyRequest, final PolicyStatusEnum oldStatus, final PolicyStatusEnum status) {
        return policyRequestRepository.updateStatus(policyRequest,
                oldStatus,
                status);
    }

    private Mono<PolicyRequest> callUpdateStatus(final PolicyRequest policyRequest) {
        return policyRequestRepository.save(policyRequest);
    }
}