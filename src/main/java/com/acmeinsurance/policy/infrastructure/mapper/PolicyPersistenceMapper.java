package com.acmeinsurance.policy.infrastructure.mapper;

import com.acmeinsurance.policy.domain.model.PolicyRequest;
import com.acmeinsurance.policy.domain.model.StatusHistoryEntry;
import com.acmeinsurance.policy.enums.CategoryEnum;
import com.acmeinsurance.policy.enums.SalesChannelEnum;
import com.acmeinsurance.policy.enums.PaymentMethodEnum;
import com.acmeinsurance.policy.enums.PolicyStatusEnum;
import com.acmeinsurance.policy.infrastructure.persistence.dynamodb.PolicyRequestDynamoDbEntity;
import com.acmeinsurance.policy.infrastructure.persistence.dynamodb.StatusHistoryEntryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PolicyPersistenceMapper {

    PolicyPersistenceMapper INSTANCE = Mappers.getMapper(PolicyPersistenceMapper.class);

    @Mapping(target = "category", source = "category.value")
    @Mapping(target = "salesChannel", source = "salesChannel.value")
    @Mapping(target = "paymentMethod", source = "paymentMethod.value")
    @Mapping(target = "status", source = "status.value")
    PolicyRequestDynamoDbEntity toEntity(PolicyRequest domain);

    @Mapping(target = "category", expression = "java(CategoryEnum.fromValue(entity.getCategory()))")
    @Mapping(target = "salesChannel", expression = "java(SalesChannelEnum.fromValue(entity.getSalesChannel()))")
    @Mapping(target = "paymentMethod", expression = "java(PaymentMethodEnum.fromValue(entity.getPaymentMethod()))")
    @Mapping(target = "status", expression = "java(PolicyStatusEnum.fromValue(entity.getStatus()))")
    PolicyRequest toDomain(PolicyRequestDynamoDbEntity entity);

    StatusHistoryEntryEntity toEntity(StatusHistoryEntry domain);

    StatusHistoryEntry toDomain(StatusHistoryEntryEntity entity);
}