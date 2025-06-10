package com.acmeinsurance.order.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PolicyStatusEnum {
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

    public static PolicyStatusEnum getNewStatus(final PolicyStatusEnum oldStatus, final String paymentConfirmed,
            final String subscriptionAuthorized) {

        switch (oldStatus) {
            case VALIDATED:
                if (StatusEnum.WAITING.equals(StatusEnum.fromValue(paymentConfirmed)) ||
                        StatusEnum.WAITING.equals(StatusEnum.fromValue(subscriptionAuthorized))) {
                    return PolicyStatusEnum.PENDING;
                } else if (StatusEnum.DENIED.equals(StatusEnum.fromValue(paymentConfirmed)) ||
                           StatusEnum.FAILED.equals(StatusEnum.fromValue(paymentConfirmed))    ||
                           StatusEnum.DENIED.equals(StatusEnum.fromValue(subscriptionAuthorized)) ||
                           StatusEnum.FAILED.equals(StatusEnum.fromValue(subscriptionAuthorized))
                ) {
                    return PolicyStatusEnum.REJECTED;
                }
                break;
            case PENDING:
                if (StatusEnum.WAITING.equals(StatusEnum.fromValue(paymentConfirmed)) ||
                        StatusEnum.WAITING.equals(StatusEnum.fromValue(subscriptionAuthorized))) {
                    return PolicyStatusEnum.PENDING;
                } else if (StatusEnum.DENIED.equals(StatusEnum.fromValue(paymentConfirmed)) ||
                        StatusEnum.FAILED.equals(StatusEnum.fromValue(paymentConfirmed))    ||
                        StatusEnum.DENIED.equals(StatusEnum.fromValue(subscriptionAuthorized)) ||
                        StatusEnum.FAILED.equals(StatusEnum.fromValue(subscriptionAuthorized))
                ) {
                    return PolicyStatusEnum.REJECTED;
                } else if(StatusEnum.CONFIRMED.equals(StatusEnum.fromValue(paymentConfirmed)) &&
                        StatusEnum.CONFIRMED.equals(StatusEnum.fromValue(subscriptionAuthorized))){

                    return PolicyStatusEnum.APPROVED;
                }
                break;
            case RECEIVED:  return PolicyStatusEnum.RECEIVED;
            case REJECTED:  return PolicyStatusEnum.REJECTED;
            case APPROVED:  return PolicyStatusEnum.APPROVED;
            case CANCELLED: return PolicyStatusEnum.CANCELLED;
        }
        return oldStatus;
    }
}