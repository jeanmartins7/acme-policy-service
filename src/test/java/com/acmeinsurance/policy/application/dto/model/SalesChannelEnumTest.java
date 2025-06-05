package com.acmeinsurance.policy.application.dto.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesChannelEnumTest {

    @ParameterizedTest
    @MethodSource("provideSalesChannelFromValues")
    @DisplayName("Should convert various string inputs to correct SalesChannel enum (case-insensitive)")
    void fromValueShouldReturnCorrectSalesChannel(final String inputValue, final SalesChannelEnum expectedEnum) {
        assertEquals(expectedEnum, SalesChannelEnum.fromValue(inputValue),
                () -> "Input '" + inputValue + "' should convert to " + expectedEnum.name());
    }

    private static Stream<Arguments> provideSalesChannelFromValues() {
        return Stream.of(
                Arguments.of("MOBILE", SalesChannelEnum.MOBILE),
                Arguments.of("mobile", SalesChannelEnum.MOBILE),

                Arguments.of("WHATSAPP", SalesChannelEnum.WHATSAPP),
                Arguments.of("whatsapp", SalesChannelEnum.WHATSAPP),

                Arguments.of("WEBSITE", SalesChannelEnum.WEBSITE),
                Arguments.of("website", SalesChannelEnum.WEBSITE),

                Arguments.of("PARTNER", SalesChannelEnum.PARTNER),
                Arguments.of("partner", SalesChannelEnum.PARTNER)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSalesChannelGetValues")
    @DisplayName("Should return correct string value for each SalesChannel enum")
    void getValueShouldReturnCorrectString(final SalesChannelEnum salesChannelEnum, final String expectedStringValue) {
        assertEquals(expectedStringValue, salesChannelEnum.getValue(),
                () -> salesChannelEnum.name() + " enum value should be '" + expectedStringValue + "'");
    }

    private static Stream<Arguments> provideSalesChannelGetValues() {
        return Stream.of(
                Arguments.of(SalesChannelEnum.MOBILE, "MOBILE"),
                Arguments.of(SalesChannelEnum.WHATSAPP, "WHATSAPP"),
                Arguments.of(SalesChannelEnum.WEBSITE, "WEBSITE"),
                Arguments.of(SalesChannelEnum.PARTNER, "PARTNER")
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid SalesChannel value")
    void fromValueShouldThrowExceptionForInvalidValue() {
        final String invalidChannel = "INVALID_CHANNEL";
        assertThrows(IllegalArgumentException.class, () -> SalesChannelEnum.fromValue(invalidChannel),
                () -> "Invalid sales channel value: " + invalidChannel + " should throw IllegalArgumentException");
    }
}