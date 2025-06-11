package com.acmeinsurance.order.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SalesChannelEnum {
    MOBILE("MOBILE"),
    WHATSAPP("WHATSAPP"),
    WEBSITE("WEBSITE"),
    PARTNER("PARTNER");

    private final String value;

    SalesChannelEnum(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SalesChannelEnum fromValue(final String value) {
        for (SalesChannelEnum channel : SalesChannelEnum.values()) {
            if (channel.value.equalsIgnoreCase(value)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Invalid SalesChannel value: " + value);
    }
}