package com.acmeinsurance.policy.domain.usecase.impl;

import com.acmeinsurance.policy.domain.model.PolicyRequest;
import com.acmeinsurance.policy.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.policy.domain.usecase.GetPolicyRequestUseCase;
import com.acmeinsurance.policy.handler.PolicyRequestNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetPolicyRequestUseCaseImpl implements GetPolicyRequestUseCase {

    private final PolicyRequestRepository policyRequestRepository;

    @Override
    public Mono<PolicyRequest> executeById(final String id) {
        return policyRequestRepository.findById(id)
                .switchIfEmpty(Mono.error(new PolicyRequestNotFoundException("Policy request with ID " + id + " not found.")));
    }

    @Override
    public Flux<PolicyRequest> executeByCustomerId(final String customerId) {
        return policyRequestRepository.findByCustomerId(customerId);
    }
}