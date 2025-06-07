package com.acmeinsurance.policy.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClassificationEnum {
    REGULAR("REGULAR"),
    HIGH_RISK("HIGH_RISK"),
    PREFERENTIAL("PREFERENTIAL"),
    SEM_INFORMACAO("SEM_INFORMACAO");

    private final String value;

    ClassificationEnum(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ClassificationEnum fromValue(final String value) {
        for (ClassificationEnum classification : ClassificationEnum.values()) {
            if (classification.value.equalsIgnoreCase(value)) {
                return classification;
            }
        }
        throw new IllegalArgumentException("Invalid FraudClassification value: " + value);
    }
}