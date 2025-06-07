package com.acmeinsurance.policy.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethodEnum {
    CREDIT_CARD("CREDIT_CARD"),
    DEBIT_CARD("DEBIT_CARD"),
    DEBIT("DEBIT"),
    BOLETO("BOLETO"),
    PIX("PIX");

    private final String value;

    PaymentMethodEnum(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PaymentMethodEnum fromValue(final String value) {
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            if (method.value.equalsIgnoreCase(value)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Invalid PaymentMethod value: " + value);
    }
}