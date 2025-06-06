package com.acmeinsurance.policy.application.service;

import com.acmeinsurance.policy.application.dto.request.PolicyRequestDTO;
import com.acmeinsurance.policy.application.dto.response.PolicyRequestResponseDTO;
import com.acmeinsurance.policy.application.dto.response.PolicyResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolicyRequestFacade {

    Mono<PolicyRequestResponseDTO> createPolicyRequest(final PolicyRequestDTO policyRequestDTO);
    Mono<PolicyResponseDTO> getPolicyRequestById(final String id);
    Flux<PolicyResponseDTO> getPolicyRequestsByCustomerId(final String customerId);


}