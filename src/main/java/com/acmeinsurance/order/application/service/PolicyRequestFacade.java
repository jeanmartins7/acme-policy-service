package com.acmeinsurance.order.application.service;

import com.acmeinsurance.order.application.dto.policy.request.PolicyRequestDTO;
import com.acmeinsurance.order.application.dto.policy.response.PolicyRequestResponseDTO;
import com.acmeinsurance.order.application.dto.policy.response.PolicyResponseDTO;
import com.acmeinsurance.order.application.mapper.PolicyRequestMapper;
import com.acmeinsurance.order.domain.usecase.CreatePolicyRequestUseCase;
import com.acmeinsurance.order.domain.usecase.GetPolicyRequestUseCase;
import com.acmeinsurance.order.domain.usecase.command.CreatePolicyRequestCommand;
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