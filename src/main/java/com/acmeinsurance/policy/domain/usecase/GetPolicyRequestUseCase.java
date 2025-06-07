package com.acmeinsurance.policy.domain.usecase;

import com.acmeinsurance.policy.domain.model.PolicyRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GetPolicyRequestUseCase {

    Mono<PolicyRequest> executeById(final String id);
    Flux<PolicyRequest> executeByCustomerId(final String customerId);

}