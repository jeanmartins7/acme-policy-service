package com.acmeinsurance.order.infrastructure.persistence.dynamodb;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.domain.service.PolicyStatusNotifier;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import com.acmeinsurance.order.infrastructure.mapper.PolicyPersistenceMapper;
import com.acmeinsurance.order.infrastructure.persistence.entity.PolicyRequestDynamoDbEntity;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class DynamoDbPolicyRequestRepositoryAdapter implements PolicyRequestRepository {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbPolicyRequestRepositoryAdapter.class);

    private final DynamoDbEnhancedAsyncClient enhancedClient;
    private final PolicyPersistenceMapper mapper;
    private final PolicyStatusNotifier policyStatusNotifier;

    private static final String TABLE_NAME = "PolicyRequests";

    private DynamoDbAsyncTable<PolicyRequestDynamoDbEntity> getTable() {
        return enhancedClient.table(TABLE_NAME, TableSchema.fromBean(PolicyRequestDynamoDbEntity.class));
    }

    @Override
    public Mono<PolicyRequest> save(final PolicyRequest request) {
        final PolicyRequestDynamoDbEntity entity = mapper.toEntity(request);
        return Mono.fromFuture(getTable().putItem(entity))
                .thenReturn(request)
                .doOnError(e -> log.error("Error saving policy {}: {}", request.getId(), e.getMessage(), e))
                .onErrorMap(e -> {
                    if (e instanceof DynamoDbException) {
                        return new RuntimeException("Failed to save policy " + request.getId() + " due to DynamoDB error: " + e.getMessage(), e);
                    }
                    return new RuntimeException("Failed to save policy " + request.getId() + " due to unexpected error: " + e.getMessage(), e);
                });
    }

    @Override
    public Mono<PolicyRequest> findById(final String id) {
        final Key key = Key.builder().partitionValue(id).build();

        return Mono.fromFuture(getTable().getItem(r -> r.key(key)))
                .filter(Objects::nonNull)
                .map(mapper::toDomain)
                .doOnError(e -> log.error("Error finding policy by ID {}: {}", id, e.getMessage(), e))
                .onErrorResume(e -> {
                    if (e instanceof ResourceNotFoundException) {
                        log.warn("Policy with ID {} not found (ResourceNotFoundException). Returning empty.", id);
                        return Mono.empty();
                    }
                    if (e instanceof DynamoDbException) {
                        log.error("DynamoDB service error when finding policy by ID {}: {}", id, e.getMessage());
                        return Mono.error(new RuntimeException("Database error when finding policy by ID " + id, e));
                    }
                    return Mono.error(new RuntimeException("Unexpected error when finding policy by ID " + id + ": " + e.getMessage(), e));
                });
    }

    @Override
    public Flux<PolicyRequest> findByCustomerId(final String customerId) {
        final QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(customerId).build());

        return Flux.from(getTable().index("customer_id-index").query(queryConditional))
                .flatMap(page -> Flux.fromIterable(page.items()))
                .map(mapper::toDomain)
                .doOnError(e -> log.error("Error querying policies by customer ID {}: {}", customerId, e.getMessage(), e))
                .onErrorResume(e -> {
                    if (e instanceof DynamoDbException) {
                        log.error("DynamoDB service error when querying policies by customer ID {}: {}", customerId, e.getMessage());
                        return Flux.error(new RuntimeException("Database error when querying policies by customer ID " + customerId, e));
                    }
                    return Flux.error(new RuntimeException("Unexpected error when querying policies by customer ID " + customerId + ": " + e.getMessage(), e));
                });
    }

    @Override
    public Mono<PolicyRequest> updateStatus(final PolicyRequest policyRequest, final PolicyStatusEnum oldStatus, final PolicyStatusEnum newStatus) {
        policyRequest.updateStatus(newStatus);

        return this.save(policyRequest)
                .flatMap(updatedPolicy -> policyStatusNotifier.notifyPolicyStatusChanged(updatedPolicy, oldStatus, newStatus)
                        .thenReturn(updatedPolicy))
                .doOnError(e -> log.error("Error updating status for policy {}: {}", policyRequest.getId(), e.getMessage(), e))
                .onErrorMap(e -> {
                    if (e instanceof DynamoDbException) {
                        return new RuntimeException("Failed to update status for policy " + policyRequest.getId() + " due to DynamoDB error: " + e.getMessage(), e);
                    }
                    return new RuntimeException("Failed to update status for policy " + policyRequest.getId() + " due to unexpected error: " + e.getMessage(), e);
                });
    }
}