package com.acmeinsurance.policy.domain.usecase;

import com.acmeinsurance.policy.domain.model.PolicyRequest;
import com.acmeinsurance.policy.domain.usecase.command.CreatePolicyRequestCommand;
import reactor.core.publisher.Mono;

public interface CreatePolicyRequestUseCase {
    Mono<PolicyRequest> execute(final CreatePolicyRequestCommand command);
}