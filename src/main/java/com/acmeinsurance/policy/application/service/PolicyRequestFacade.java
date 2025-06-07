package com.acmeinsurance.policy.application.service;

import com.acmeinsurance.policy.application.dto.request.PolicyRequestDTO;
import com.acmeinsurance.policy.application.dto.response.PolicyRequestResponseDTO;
import com.acmeinsurance.policy.application.dto.response.PolicyResponseDTO;
import com.acmeinsurance.policy.application.mapper.PolicyRequestMapper;
import com.acmeinsurance.policy.domain.usecase.CreatePolicyRequestUseCase;
import com.acmeinsurance.policy.domain.usecase.GetPolicyRequestUseCase;
import com.acmeinsurance.policy.domain.usecase.command.CreatePolicyRequestCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PolicyRequestFacade {

    private final PolicyRequestMapper policyRequestMapper;
    private final CreatePolicyRequestUseCase createPolicyRequestUseCase;
    private final GetPolicyRequestUseCase getPolicyRequestUseCase;

    public Mono<PolicyRequestResponseDTO> createPolicyRequest(final PolicyRequestDTO requestDTO) {
        final CreatePolicyRequestCommand command = policyRequestMapper.toCreateCommand(requestDTO);

        return createPolicyRequestUseCase.execute(command)
                .map(policyRequestMapper::toCreationResponseDTO);
    }

    public Mono<PolicyResponseDTO> getPolicyRequestById(final String id) {

        return getPolicyRequestUseCase.executeById(id)
                .map(policyRequestMapper::toResponseDTO);
    }

    public Flux<PolicyResponseDTO> getPolicyRequestsByCustomerId(final String customerId) {

        return getPolicyRequestUseCase.executeByCustomerId(customerId)
                .map(policyRequestMapper::toResponseDTO);
    }
}