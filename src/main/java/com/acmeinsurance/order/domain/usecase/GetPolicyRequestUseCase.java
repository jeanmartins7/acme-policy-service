package com.acmeinsurance.order.domain.usecase;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GetPolicyRequestUseCase {

    Mono<PolicyRequest> executeById(final String id);
    Flux<PolicyRequest> executeByCustomerId(final String customerId);

}