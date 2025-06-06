package com.acmeinsurance.policy.application.dto.enums;

import com.acmeinsurance.policy.domain.enums.PolicyStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

class PolicyStatusEnumTest {

    @ParameterizedTest
    @MethodSource("providePolicyStatusFromValues")
    @DisplayName("Should convert various string inputs to correct PolicyStatus enum (case-insensitive)")
    void fromValueShouldReturnCorrectPolicyStatus(final String inputValue, final PolicyStatusEnum expectedEnum) {
        assertEquals(expectedEnum, PolicyStatusEnum.fromValue(inputValue),
                () -> "Input '" + inputValue + "' should convert to " + expectedEnum.name());
    }

    private static Stream<Arguments> providePolicyStatusFromValues() {
        return Stream.of(
                of("RECEIVED", PolicyStatusEnum.RECEIVED),
                of("received", PolicyStatusEnum.RECEIVED),

                of("VALIDATED", PolicyStatusEnum.VALIDATED),
                of("validated", PolicyStatusEnum.VALIDATED),

                of("PENDING", PolicyStatusEnum.PENDING),
                of("pending", PolicyStatusEnum.PENDING),

                of("APPROVED", PolicyStatusEnum.APPROVED),
                of("approved", PolicyStatusEnum.APPROVED),

                of("REJECTED", PolicyStatusEnum.REJECTED),
                of("rejected", PolicyStatusEnum.REJECTED),

                of("CANCELLED", PolicyStatusEnum.CANCELLED),
                of("cancelled", PolicyStatusEnum.CANCELLED)
        );
    }

    @ParameterizedTest
    @MethodSource("providePolicyStatusGetValues")
    @DisplayName("Should return correct string value for each PolicyStatus enum")
    void getValueShouldReturnCorrectString(final PolicyStatusEnum statusEnum, final String expectedStringValue) {
        assertEquals(expectedStringValue, statusEnum.getValue(),
                () -> statusEnum.name() + " enum value should be '" + expectedStringValue + "'");
    }

    private static Stream<Arguments> providePolicyStatusGetValues() {
        return Stream.of(
                of(PolicyStatusEnum.RECEIVED, "RECEIVED"),
                of(PolicyStatusEnum.VALIDATED, "VALIDATED"),
                of(PolicyStatusEnum.PENDING, "PENDING"),
                of(PolicyStatusEnum.APPROVED, "APPROVED"),
                of(PolicyStatusEnum.REJECTED, "REJECTED"),
                of(PolicyStatusEnum.CANCELLED, "CANCELLED")
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid PolicyStatus value")
    void fromValueShouldThrowExceptionForInvalidValue() {
        final String invalidStatus = "INVALID_STATUS";
        assertThrows(IllegalArgumentException.class, () -> PolicyStatusEnum.fromValue(invalidStatus),
                "Invalid status value: " + invalidStatus + " should throw IllegalArgumentException");
    }
}