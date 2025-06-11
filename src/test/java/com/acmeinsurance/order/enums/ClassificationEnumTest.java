package com.acmeinsurance.order.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassificationEnumTest {

    @ParameterizedTest
    @MethodSource("provideClassificationFromValues")
    @DisplayName("Should convert string value to correct FraudClassification enum (case-insensitive)")
    void fromValueShouldReturnCorrectClassification(final String inputValue, final ClassificationEnum expectedEnum) {
        assertEquals(expectedEnum, ClassificationEnum.fromValue(inputValue),
                () -> "Input '" + inputValue + "' should convert to " + expectedEnum.name());
    }

    private static Stream<Arguments> provideClassificationFromValues() {
        return Stream.of(
                Arguments.of("REGULAR", ClassificationEnum.REGULAR),
                Arguments.of("regular", ClassificationEnum.REGULAR),

                Arguments.of("HIGH_RISK", ClassificationEnum.HIGH_RISK),
                Arguments.of("high_risk", ClassificationEnum.HIGH_RISK),

                Arguments.of("PREFERENTIAL", ClassificationEnum.PREFERENTIAL),
                Arguments.of("preferential", ClassificationEnum.PREFERENTIAL)
        );
    }

    @ParameterizedTest
    @MethodSource("provideClassificationGetValues")
    @DisplayName("Should return correct string value for each FraudClassification enum")
    void getValueShouldReturnCorrectString(final ClassificationEnum classificationEnum, final String expectedStringValue) {
        assertEquals(expectedStringValue, classificationEnum.getValue(),
                () -> classificationEnum.name() + " enum value should be '" + expectedStringValue + "'");
    }

    private static Stream<Arguments> provideClassificationGetValues() {
        return Stream.of(
                Arguments.of(ClassificationEnum.REGULAR, "REGULAR"),
                Arguments.of(ClassificationEnum.HIGH_RISK, "HIGH_RISK"),
                Arguments.of(ClassificationEnum.PREFERENTIAL, "PREFERENTIAL")
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid FraudClassification value")
    void fromValueShouldThrowExceptionForInvalidValue() {
        final String invalidClassification = "INVALID_CLASSIFICATION";
        assertThrows(IllegalArgumentException.class, () -> ClassificationEnum.fromValue(invalidClassification),
                "Invalid classification value: " + invalidClassification + " should throw IllegalArgumentException");
    }
}