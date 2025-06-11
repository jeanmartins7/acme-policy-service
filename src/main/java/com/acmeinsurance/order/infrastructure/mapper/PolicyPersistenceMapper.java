package com.acmeinsurance.order.infrastructure.mapper;

import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.model.StatusHistoryEntry;
import com.acmeinsurance.order.infrastructure.persistence.entity.PolicyRequestDynamoDbEntity;
import com.acmeinsurance.order.infrastructure.persistence.entity.StatusHistoryEntryEntity;
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
    @Mapping(target = "paymentConfirmed", source = "paymentConfirmed")
    @Mapping(target = "subscriptionAuthorized", source = "subscriptionAuthorized")
    PolicyRequestDynamoDbEntity toEntity(PolicyRequest domain);

    @Mapping(target = "category", expression = "java(CategoryEnum.fromValue(entity.getCategory()))")
    @Mapping(target = "salesChannel", expression = "java(SalesChannelEnum.fromValue(entity.getSalesChannel()))")
    @Mapping(target = "paymentMethod", expression = "java(PaymentMethodEnum.fromValue(entity.getPaymentMethod()))")
    @Mapping(target = "status", expression = "java(PolicyStatusEnum.fromValue(entity.getStatus()))")
    PolicyRequest toDomain(PolicyRequestDynamoDbEntity entity);

    StatusHistoryEntryEntity toEntity(StatusHistoryEntry domain);

    StatusHistoryEntry toDomain(StatusHistoryEntryEntity entity);
}