package com.acmeinsurance.order.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentMethodEnumTest {

    @ParameterizedTest
    @MethodSource("providePaymentMethodFromValues")
    @DisplayName("Should convert various string inputs to correct PaymentMethod enum (case-insensitive)")
    void fromValueShouldReturnCorrectPaymentMethod(final String inputValue, final PaymentMethodEnum expectedEnum) {
        assertEquals(expectedEnum, PaymentMethodEnum.fromValue(inputValue),
                () -> "Input '" + inputValue + "' should convert to " + expectedEnum.name());
    }

    private static Stream<Arguments> providePaymentMethodFromValues() {
        return Stream.of(
                Arguments.of("CREDIT_CARD", PaymentMethodEnum.CREDIT_CARD),
                Arguments.of("credit_card", PaymentMethodEnum.CREDIT_CARD),

                Arguments.of("DEBIT_CARD", PaymentMethodEnum.DEBIT_CARD),
                Arguments.of("debit_card", PaymentMethodEnum.DEBIT_CARD),

                Arguments.of("BOLETO", PaymentMethodEnum.BOLETO),
                Arguments.of("boleto", PaymentMethodEnum.BOLETO),

                Arguments.of("PIX", PaymentMethodEnum.PIX),
                Arguments.of("pix", PaymentMethodEnum.PIX)
        );
    }

    @ParameterizedTest
    @MethodSource("providePaymentMethodGetValues")
    @DisplayName("Should return correct string value for each PaymentMethod enum")
    void getValueShouldReturnCorrectString(final PaymentMethodEnum paymentMethodEnum, final String expectedStringValue) {
        assertEquals(expectedStringValue, paymentMethodEnum.getValue(),
                () -> paymentMethodEnum.name() + " enum value should be '" + expectedStringValue + "'");
    }

    private static Stream<Arguments> providePaymentMethodGetValues() {
        return Stream.of(
                Arguments.of(PaymentMethodEnum.CREDIT_CARD, "CREDIT_CARD"),
                Arguments.of(PaymentMethodEnum.DEBIT_CARD, "DEBIT_CARD"),
                Arguments.of(PaymentMethodEnum.BOLETO, "BOLETO"),
                Arguments.of(PaymentMethodEnum.PIX, "PIX")
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid PaymentMethod value")
    void fromValueShouldThrowExceptionForInvalidValue() {
        final String invalidMethod = "INVALID_METHOD";
        assertThrows(IllegalArgumentException.class, () -> PaymentMethodEnum.fromValue(invalidMethod),
                "Invalid payment method value: " + invalidMethod + " should throw IllegalArgumentException");
    }
}