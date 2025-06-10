package com.acmeinsurance.order.infrastructure.persistence.converter;

import com.acmeinsurance.order.infrastructure.persistence.entity.StatusHistoryEntryEntity;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.stream.Collectors;

public class StatusHistoryEntryListConverter implements AttributeConverter<List<StatusHistoryEntryEntity>> {

    private static final TableSchema<StatusHistoryEntryEntity> SCHEMA =
            TableSchema.fromBean(StatusHistoryEntryEntity.class);

    public StatusHistoryEntryListConverter() {}

    @Override
    public AttributeValue transformFrom(final List<StatusHistoryEntryEntity> input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }

        List<AttributeValue> listAttributeValue = input.stream()
                .map(item -> AttributeValue.builder().m(SCHEMA.itemToMap(item, true)).build())
                .collect(Collectors.toList());
        return AttributeValue.builder().l(listAttributeValue).build();
    }

    @Override
    public List<StatusHistoryEntryEntity> transformTo(final AttributeValue input) {
        if (input == null || input.l() == null) {
            return null;
        }

        return input.l().stream()
                .map(AttributeValue::m)
                .map(SCHEMA::mapToItem)
                .collect(Collectors.toList());
    }

    @Override
    public EnhancedType<List<StatusHistoryEntryEntity>> type() {

        return EnhancedType.listOf(StatusHistoryEntryEntity.class);

    }

    @Override
    public AttributeValueType attributeValueType() {

        return AttributeValueType.L;

    }
}