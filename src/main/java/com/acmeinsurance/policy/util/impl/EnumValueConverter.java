package com.acmeinsurance.policy.util.impl;

import com.acmeinsurance.policy.util.IValueEnum;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Arrays;

public class EnumValueConverter<T extends Enum<T> & IValueEnum> implements AttributeConverter<T> {

    private final Class<T> enumClass;

    public EnumValueConverter(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public AttributeValue transformFrom(final T input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        return AttributeValue.builder().s(input.getValue()).build();
    }

    @Override
    public T transformTo(final AttributeValue input) {
        if (input == null || input.s() == null) {
            return null;
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getValue().equalsIgnoreCase(input.s()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid enum value: " + input.s() + " for enum " + enumClass.getSimpleName()));
    }

    @Override
    public EnhancedType<T> type() {
        return EnhancedType.of(enumClass);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}