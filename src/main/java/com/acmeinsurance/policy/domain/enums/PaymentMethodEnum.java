package com.acmeinsurance.policy.domain.enums;

import com.acmeinsurance.policy.util.IValueEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethodEnum implements IValueEnum {
    CREDIT_CARD("CREDIT_CARD"),
    DEBIT_CARD("DEBIT_CARD"),
    DEBIT("DEBIT"),
    BOLETO("BOLETO"),
    PIX("PIX");

    private final String value;

    PaymentMethodEnum(final String value) {
        this.value = value;
    }

    @Override
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