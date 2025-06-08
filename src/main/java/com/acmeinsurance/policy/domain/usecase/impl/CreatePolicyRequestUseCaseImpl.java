package com.acmeinsurance.policy.domain.usecase.impl;

import com.acmeinsurance.policy.avro.PolicyReceivedEvent;
import com.acmeinsurance.policy.domain.model.PolicyRequest;
import com.acmeinsurance.policy.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.policy.domain.usecase.CreatePolicyRequestUseCase;
import com.acmeinsurance.policy.domain.usecase.command.CreatePolicyRequestCommand;
import com.acmeinsurance.policy.infrastructure.kafka.PolicyEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;


@Service
@RequiredArgsConstructor
public class CreatePolicyRequestUseCaseImpl implements CreatePolicyRequestUseCase {

    private static final String POLICY_RECEIVED = "POLICY_RECEIVED";
    private final PolicyRequestRepository policyRequestRepository;
    private final PolicyEventPublisher policyEventPublisher;

    @Override
    public Mono<PolicyRequest> execute(final CreatePolicyRequestCommand command) {

        final PolicyRequest newPolicyRequest = PolicyRequest.createInitialRequest(
                command.getCustomerId(),
                command.getProductId(),
                command.getCategory(),
                command.getSalesChannel(),
                command.getPaymentMethod(),
                command.getTotalMonthlyPremiumAmount(),
                command.getInsuredAmount(),
                command.getCoverages(),
                command.getAssistances()
        );

        return policyRequestRepository.save(newPolicyRequest)
                .flatMap(this::sendPolicyRequest);
    }

    private Mono<PolicyRequest> sendPolicyRequest(final PolicyRequest savedPolicy) {

        final PolicyReceivedEvent avroEvent = PolicyReceivedEvent.newBuilder()
                .setPolicyId(savedPolicy.getId().toString())
                .setCustomerId(savedPolicy.getCustomerId().toString())
                .setProductId(savedPolicy.getProductId())
                .setCategory(savedPolicy.getCategory().getValue())
                .setSalesChannel(savedPolicy.getSalesChannel().getValue())
                .setPaymentMethod(savedPolicy.getPaymentMethod().getValue())
                .setTotalMonthlyPremiumAmount(savedPolicy.getTotalMonthlyPremiumAmount())
                .setInsuredAmount(savedPolicy.getInsuredAmount())
                .setCreatedAt(Instant.ofEpochSecond(savedPolicy.getCreatedAt().toEpochMilli()))
                .build();

        return policyEventPublisher.publishPolicyReceivedEvent(avroEvent, POLICY_RECEIVED)
                .thenReturn(savedPolicy);
    }
}
