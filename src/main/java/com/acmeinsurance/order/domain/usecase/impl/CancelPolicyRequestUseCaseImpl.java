package com.acmeinsurance.order.domain.usecase.impl;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.domain.strategy.policyStatusStrategy.PolicyStatusValidationChain;
import com.acmeinsurance.order.domain.usecase.CancelPolicyRequestUseCase;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class CancelPolicyRequestUseCaseImpl implements CancelPolicyRequestUseCase {

    private final PolicyRequestRepository policyRequestRepository;
    private final PolicyStatusValidationChain validationChain;

    @Override
    public Mono<PolicyRequest> executeById(final String policyId) {

        return policyRequestRepository.findById(policyId)
                .flatMap(policyRequest ->
                        validationChain.validateCancel(policyRequest)
                                .flatMap(validatedPolicyRequest ->
                                        policyRequestRepository.updateStatus(validatedPolicyRequest,
                                                validatedPolicyRequest.getStatus(),
                                                PolicyStatusEnum.CANCELLED))
                );
    }
}