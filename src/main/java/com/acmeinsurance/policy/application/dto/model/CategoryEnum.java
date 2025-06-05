package com.acmeinsurance.policy.application.dto.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CategoryEnum {
    AUTO("AUTO"),
    VIDA("VIDA"),
    RESIDENCIAL("RESIDENCIAL"),
    EMPRESARIAL("EMPRESARIAL");

    private final String value;

    CategoryEnum(final String value) {
        this.value = value;
    }

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