package com.acmeinsurance.order.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusEnum {
    WAITING("WAITING"),
    CONFIRMED("CONFIRMED"),
    FAILED("FAILED"),
    DENIED("DENIED");

    private final String value;

    StatusEnum(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static StatusEnum fromValue(final String value) {
        for (StatusEnum status : StatusEnum.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }

            if ("true".equalsIgnoreCase(value)) {
                return CONFIRMED;
            }
            if ("false".equalsIgnoreCase(value)) {
                return DENIED;
            }
        }
        throw new IllegalArgumentException("Invalid StatusEnum value: " + value);
    }

    @JsonCreator
    public static StatusEnum fromValue(final boolean value) {

        if (value) {
            return CONFIRMED;
        }

        return DENIED;

    }
}