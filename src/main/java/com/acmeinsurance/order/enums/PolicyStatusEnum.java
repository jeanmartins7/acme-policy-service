package com.acmeinsurance.order.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum PolicyStatusEnum {
    RECEIVED("RECEIVED"),
    VALIDATED("VALIDATED"),
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    CANCELLED("CANCELLED"),
    UNKNOWN("UNKNOWN");

    private final String value;

    PolicyStatusEnum(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PolicyStatusEnum fromValue(final String value) {

        if (value == null) {
            return UNKNOWN;
        }
        return Arrays.stream(PolicyStatusEnum.values())
                .filter(status -> status.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNKNOWN);
    }
}