package com.acmeinsurance.policy.domain.model;

import com.acmeinsurance.policy.application.dto.model.CategoryEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoryEnumTest {


    @ParameterizedTest
    @MethodSource("provideCategoryFromValues")
    @DisplayName("Should convert various string inputs to correct Category enum (case-insensitive)")
    void fromValueShouldReturnCorrectCategory(final String inputValue, final CategoryEnum expectedEnum) {
        assertEquals(expectedEnum, CategoryEnum.fromValue(inputValue),
                () -> "Input '" + inputValue + "' should convert to " + expectedEnum.name());
    }

    private static Stream<Arguments> provideCategoryFromValues() {
        return Stream.of(
                Arguments.of("AUTO", CategoryEnum.AUTO),
                Arguments.of("auto", CategoryEnum.AUTO),

                Arguments.of("VIDA", CategoryEnum.VIDA),
                Arguments.of("vida", CategoryEnum.VIDA),

                Arguments.of("RESIDENCIAL", CategoryEnum.RESIDENCIAL),
                Arguments.of("Residencial", CategoryEnum.RESIDENCIAL),

                Arguments.of("EMPRESARIAL", CategoryEnum.EMPRESARIAL),
                Arguments.of("Empresarial", CategoryEnum.EMPRESARIAL)
        );
    }

    @ParameterizedTest
    @MethodSource("provideCategoryGetValues")
    @DisplayName("Should return correct string value for each Category enum")
    void getValueShouldReturnCorrectString(final CategoryEnum categoryEnum, final String expectedStringValue) {
        assertEquals(expectedStringValue, categoryEnum.getValue(),
                () -> categoryEnum.name() + " enum value should be '" + expectedStringValue + "'");
    }

    private static Stream<Arguments> provideCategoryGetValues() {
        return Stream.of(
                Arguments.of(CategoryEnum.AUTO, "AUTO"),
                Arguments.of(CategoryEnum.VIDA, "VIDA"),
                Arguments.of(CategoryEnum.RESIDENCIAL, "RESIDENCIAL"),
                Arguments.of(CategoryEnum.EMPRESARIAL, "EMPRESARIAL")
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid Category value")
    void fromValueShouldThrowExceptionForInvalidValue() {
        String invalidCategory = "INVALID_CATEGORY";
        assertThrows(IllegalArgumentException.class, () -> CategoryEnum.fromValue(invalidCategory),
                () -> "Invalid category value: " + invalidCategory + " should throw IllegalArgumentException");
    }
}