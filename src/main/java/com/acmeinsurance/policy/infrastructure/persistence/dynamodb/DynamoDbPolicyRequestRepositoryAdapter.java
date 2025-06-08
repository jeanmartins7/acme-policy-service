package com.acmeinsurance.policy.infrastructure.persistence.dynamodb;

import com.acmeinsurance.policy.domain.model.PolicyRequest;
import com.acmeinsurance.policy.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.policy.infrastructure.mapper.PolicyPersistenceMapper;
import com.acmeinsurance.policy.infrastructure.persistence.entity.PolicyRequestDynamoDbEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
@RequiredArgsConstructor
public class DynamoDbPolicyRequestRepositoryAdapter implements PolicyRequestRepository {

    private final DynamoDbEnhancedAsyncClient enhancedClient;
    private final PolicyPersistenceMapper mapper;

    private static final String TABLE_NAME = "PolicyRequests";

    private DynamoDbAsyncTable<PolicyRequestDynamoDbEntity> getTable() {

        return enhancedClient.table(TABLE_NAME, TableSchema.fromBean(PolicyRequestDynamoDbEntity.class));

    }

    @Override
    public Mono<PolicyRequest> save(final PolicyRequest request) {

        final PolicyRequestDynamoDbEntity entity = mapper.toEntity(request);

        return Mono.fromFuture(getTable().putItem(entity))
                .thenReturn(request);
    }

    @Override
    public Mono<PolicyRequest> findById(final String id) {

        final Key key = Key.builder().partitionValue(id).build();

        return Mono.fromFuture(getTable().getItem(r -> r.key(key)))
                .filter(java.util.Objects::nonNull)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<PolicyRequest> findByCustomerId(final String customerId) {

        final QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(customerId).build());

        return Flux.from(getTable().index("customer_id-index").query(queryConditional))
                .flatMap(page -> Flux.fromIterable(page.items()))
                .map(mapper::toDomain);
    }
}