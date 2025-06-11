package com.acmeinsurance.order.domain.repository;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PolicyRequestRepository {

    Mono<PolicyRequest> save(final PolicyRequest request);

    Mono<PolicyRequest> findById(final String id);

    Flux<PolicyRequest> findByCustomerId(final String customerId);

    Mono<PolicyRequest> updateStatus(final PolicyRequest request, final PolicyStatusEnum oldStatus, final PolicyStatusEnum newStatus);
}