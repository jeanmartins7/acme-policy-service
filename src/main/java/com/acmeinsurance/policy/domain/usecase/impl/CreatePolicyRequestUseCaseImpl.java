package com.acmeinsurance.policy.domain.usecase.impl;

import com.acmeinsurance.policy.domain.model.PolicyRequest;
import com.acmeinsurance.policy.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.policy.domain.usecase.CreatePolicyRequestUseCase;
import com.acmeinsurance.policy.domain.usecase.command.CreatePolicyRequestCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class CreatePolicyRequestUseCaseImpl implements CreatePolicyRequestUseCase {

    private final PolicyRequestRepository policyRequestRepository;

    @Override
    public Mono<PolicyRequest> execute(final CreatePolicyRequestCommand command) {

        PolicyRequest newPolicyRequest = PolicyRequest.createInitialRequest(
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

        return policyRequestRepository.save(newPolicyRequest);
    }
}
