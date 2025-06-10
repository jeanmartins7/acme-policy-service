package com.acmeinsurance.order.domain.service;

import com.acmeinsurance.order.avro.SubscriptionProcessedEvent;
import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.acmeinsurance.order.utils.ValidateUtils.isConfirmed;
import static com.acmeinsurance.order.utils.ValidateUtils.isDenied;
import static com.acmeinsurance.order.utils.ValidateUtils.isRejected;

@Service
@RequiredArgsConstructor
public class SubscriptionProcessedService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionProcessedService.class);

    private final PolicyRequestRepository policyRequestRepository;
    private final PolicyStatusNotifier policyStatusNotifier;

    public Mono<PolicyRequest> processSubscription(final SubscriptionProcessedEvent event) {

        return policyRequestRepository.findById(event.getPolicyId())

                .flatMap(policyRequest -> {

                    if (isRejected(policyRequest.getStatus().getValue())) {
                        return Mono.just(policyRequest);
                    }

                    final PolicyStatusEnum oldStatus = policyRequest.getStatus();

                    if (isDenied(event.getStatus())) {

                        policyRequest.markSubscriptionAuthorized(false);

                        return policyRequestRepository.updateStatus(policyRequest,
                                oldStatus,
                                PolicyStatusEnum.REJECTED);
                    }

                    if (isConfirmed(event.getStatus())) {
                        policyRequest.markSubscriptionAuthorized(true);
                    }


                    log.info("Policy ID: {}. Current Status: {}. Payment outcome: {}. subscreption autorized confirmed flag: {}",
                            event.getPolicyId(), oldStatus, event.getStatus(), policyRequest.getPaymentConfirmed());

                    final PolicyStatusEnum newStatus = PolicyStatusEnum.getNewStatus(oldStatus,
                            policyRequest.getPaymentConfirmed(),
                            policyRequest.getSubscriptionAuthorized());

                    if (!newStatus.equals(oldStatus)) {

                        return policyRequestRepository.updateStatus(policyRequest,
                                oldStatus,
                                newStatus);
                    }

                    if (policyRequest.getStatus().equals(PolicyStatusEnum.PENDING) &&
                            isConfirmed(policyRequest.getPaymentConfirmed()) &&
                            isConfirmed(policyRequest.getSubscriptionAuthorized())) {

                        log.info("Policy ID: {} has both Payment CONFIRMED and Subscription AUTHORIZED. Transitioning to APPROVED.", policyRequest.getId());
                        return policyRequestRepository.updateStatus(policyRequest,
                                PolicyStatusEnum.PENDING,
                                PolicyStatusEnum.APPROVED);
                    }

                    return policyRequestRepository.save(policyRequest)
                            .thenReturn(policyRequest);
                })
                .doOnError(e -> log.error("Error processing payment outcome for policyId {}: {}", event.getPolicyId(), e.getMessage()))
                .onErrorResume(e -> {
                    log.error("Critical error in payment outcome processing for policyId {}. Marking as REJECTED for safety.", event.getPolicyId(), e);

                    return policyRequestRepository.findById(event.getPolicyId())
                            .flatMap(p -> {
                                p.updateStatus(PolicyStatusEnum.REJECTED);
                                p.markSubscriptionAuthorized(false);
                                return policyRequestRepository.save(p);
                            })
                            .flatMap(updatedPolicy -> policyStatusNotifier
                                    .notifyPolicyStatusChanged(updatedPolicy,
                                            updatedPolicy.getHistory().get(updatedPolicy.getHistory().size() - 1).getStatus(),
                                            PolicyStatusEnum.REJECTED)
                                    .thenReturn(updatedPolicy));
                });
    }

}
