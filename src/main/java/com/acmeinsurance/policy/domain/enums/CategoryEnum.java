package com.acmeinsurance.policy.domain.enums;

import com.acmeinsurance.policy.util.IValueEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CategoryEnum implements IValueEnum {
    AUTO("AUTO"),
    VIDA("VIDA"),
    RESIDENCIAL("RESIDENCIAL"),
    EMPRESARIAL("EMPRESARIAL");

    private final String value;

    CategoryEnum(final String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CategoryEnum fromValue(final String value) {
        for (CategoryEnum categoryEnum : CategoryEnum.values()) {
            if (categoryEnum.value.equalsIgnoreCase(value)) {
                return categoryEnum;
            }
        }
        throw new IllegalArgumentException("Invalid Category value: " + value);
    }
}