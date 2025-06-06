package com.acmeinsurance.policy.infrastructure.mapper;

import com.acmeinsurance.policy.domain.enums.CategoryEnum;
import com.acmeinsurance.policy.domain.enums.PaymentMethodEnum;
import com.acmeinsurance.policy.domain.enums.PolicyStatusEnum;
import com.acmeinsurance.policy.domain.enums.SalesChannelEnum;
import com.acmeinsurance.policy.domain.model.PolicyRequest;
import com.acmeinsurance.policy.domain.model.StatusHistoryEntry;
import com.acmeinsurance.policy.infrastructure.persistence.dynamodb.PolicyRequestDynamoDbEntity;
import com.acmeinsurance.policy.infrastructure.persistence.dynamodb.StatusHistoryEntryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PolicyPersistenceMapper {

    PolicyPersistenceMapper INSTANCE = Mappers.getMapper(PolicyPersistenceMapper.class);

    @Mapping(target = "category", source = "category.value")
    @Mapping(target = "salesChannel", source = "salesChannel.value")
    @Mapping(target = "paymentMethod", source = "paymentMethod.value")
    @Mapping(target = "status", source = "status.value")
    PolicyRequestDynamoDbEntity toEntity(final PolicyRequest domain);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "salesChannel", source = "salesChannel")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "productId", source = "productId") // product_id já é Long->Long
    PolicyRequest toDomain(final PolicyRequestDynamoDbEntity entity);

    StatusHistoryEntryEntity toEntity(final StatusHistoryEntry domain);
    StatusHistoryEntry toDomain(final StatusHistoryEntryEntity entity);

}