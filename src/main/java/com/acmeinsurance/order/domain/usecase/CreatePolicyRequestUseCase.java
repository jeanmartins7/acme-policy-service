package com.acmeinsurance.order.domain.usecase;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.usecase.command.CreatePolicyRequestCommand;
import reactor.core.publisher.Mono;

public interface CreatePolicyRequestUseCase {
    Mono<PolicyRequest> execute(final CreatePolicyRequestCommand command);
}