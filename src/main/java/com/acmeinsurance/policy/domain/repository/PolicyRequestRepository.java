package com.acmeinsurance.policy.domain.repository;

import com.acmeinsurance.policy.domain.model.PolicyRequest;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PolicyRequestRepository {

    Mono<PolicyRequest> save(final PolicyRequest request);

    Mono<PolicyRequest> findById(final String id);

    Flux<PolicyRequest> findByCustomerId(final String customerId);
}