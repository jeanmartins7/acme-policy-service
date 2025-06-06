package com.acmeinsurance.policy.application.service.impl;

import com.acmeinsurance.policy.application.dto.request.PolicyRequest;
import com.acmeinsurance.policy.application.dto.response.PolicyResponse;
import com.acmeinsurance.policy.application.service.PolicyFacade;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class PolicyFacadeImpl implements PolicyFacade {


    @Override
    public Mono<PolicyResponse> createPolicyRequest(final PolicyRequest request) {

        return Mono.just(PolicyResponse.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .build());
    }
}