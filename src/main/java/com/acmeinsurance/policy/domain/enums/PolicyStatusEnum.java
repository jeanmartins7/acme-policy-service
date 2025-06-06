package com.acmeinsurance.policy.domain.enums;

import com.acmeinsurance.policy.util.IValueEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PolicyStatusEnum implements IValueEnum {
    RECEIVED("RECEIVED"),
    VALIDATED("VALIDATED"),
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    CANCELLED("CANCELLED");

    private final String value;

    PolicyStatusEnum(final String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PolicyStatusEnum fromValue(final String value) {
        for (PolicyStatusEnum status : PolicyStatusEnum.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid PolicyStatus value: " + value);
    }
}