package com.acmeinsurance.order.domain.service;

import com.acmeinsurance.order.avro.SubscriptionProcessedEvent;
import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.domain.strategy.policyStatusStrategy.PolicyStatusValidationChain;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SubscriptionProcessedService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionProcessedService.class);

    private final PolicyRequestRepository policyRequestRepository;
    private final PolicyStatusValidationChain policyStatusValidationChain;

    public Mono<PolicyRequest> processSubscription(final SubscriptionProcessedEvent event) {

        return policyRequestRepository.findById(event.getPolicyId())
                .flatMap(policyRequest -> {
                    log.info("Policy ID: {}. Current Status: {}. Payment outcome: {}. Payment confirmed flag: {}",
                            policyRequest.getId(), policyRequest.getStatus(), event.getStatus(), policyRequest.getPaymentConfirmed());

                    return policyStatusValidationChain.applySubscriptionAuthorized(policyRequest, event)
                            .doOnError(e -> log.error("Error applying payment status strategy for policyId {}: {}", policyRequest.getId(), e.getMessage()));
                })
                .doOnError(e -> log.error("Error processing payment outcome for policyId {}: {}", event.getPolicyId(), e.getMessage()));
    }

}