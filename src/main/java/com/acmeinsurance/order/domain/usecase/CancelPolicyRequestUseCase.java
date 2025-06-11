package com.acmeinsurance.order.domain.usecase;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import reactor.core.publisher.Mono;

public interface CancelPolicyRequestUseCase {

    Mono<PolicyRequest> executeById(final String id);
}
