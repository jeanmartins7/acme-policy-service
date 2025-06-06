package com.acmeinsurance.policy.application.service;

import com.acmeinsurance.policy.application.dto.request.PolicyRequest;
import com.acmeinsurance.policy.application.dto.response.PolicyResponse;
import reactor.core.publisher.Mono;

public interface PolicyFacade {

    Mono<PolicyResponse> createPolicyRequest(final PolicyRequest policyRequest);

}