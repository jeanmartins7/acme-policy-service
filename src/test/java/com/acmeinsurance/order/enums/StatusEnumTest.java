package com.acmeinsurance.order.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatusEnumTest {

    @Test
    @DisplayName("Should return correct string value for each StatusEnum")
    void getValueShouldReturnCorrectString() {
        assertEquals("WAITING", StatusEnum.WAITING.getValue());
        assertEquals("CONFIRMED", StatusEnum.CONFIRMED.getValue());
        assertEquals("ACTIVE", StatusEnum.ACTIVE.getValue());
        assertEquals("INACTIVE", StatusEnum.INACTIVE.getValue());
        assertEquals("FAILED", StatusEnum.FAILED.getValue());
        assertEquals("DENIED", StatusEnum.DENIED.getValue());
    }

    @Test
    @DisplayName("Should convert string to correct StatusEnum (case-insensitive)")
    void fromValueShouldReturnCorrectEnum() {
        assertEquals(StatusEnum.WAITING, StatusEnum.fromValue("WAITING"));
        assertEquals(StatusEnum.WAITING, StatusEnum.fromValue("waiting"));
        assertEquals(StatusEnum.CONFIRMED, StatusEnum.fromValue("CONFIRMED"));
        assertEquals(StatusEnum.CONFIRMED, StatusEnum.fromValue("confirmed"));
        assertEquals(StatusEnum.ACTIVE, StatusEnum.fromValue("ACTIVE"));
        assertEquals(StatusEnum.ACTIVE, StatusEnum.fromValue("active"));
        assertEquals(StatusEnum.INACTIVE, StatusEnum.fromValue("INACTIVE"));
        assertEquals(StatusEnum.INACTIVE, StatusEnum.fromValue("inactive"));
        assertEquals(StatusEnum.FAILED, StatusEnum.fromValue("FAILED"));
        assertEquals(StatusEnum.FAILED, StatusEnum.fromValue("failed"));
        assertEquals(StatusEnum.DENIED, StatusEnum.fromValue("DENIED"));
        assertEquals(StatusEnum.DENIED, StatusEnum.fromValue("denied"));
    }

    @Test
    @DisplayName("Should convert 'true' and 'false' string to CONFIRMED and DENIED")
    void fromValueShouldHandleBooleanStrings() {
        assertEquals(StatusEnum.CONFIRMED, StatusEnum.fromValue("true"));
        assertEquals(StatusEnum.CONFIRMED, StatusEnum.fromValue("TRUE"));
        assertEquals(StatusEnum.DENIED, StatusEnum.fromValue("false"));
        assertEquals(StatusEnum.DENIED, StatusEnum.fromValue("FALSE"));
    }

    @Test
    @DisplayName("Should convert boolean to CONFIRMED and DENIED")
    void fromValueShouldHandleBoolean() {
        assertEquals(StatusEnum.CONFIRMED, StatusEnum.fromValue(true));
        assertEquals(StatusEnum.DENIED, StatusEnum.fromValue(false));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid string")
    void fromValueShouldThrowForInvalidString() {
        assertThrows(IllegalArgumentException.class, () -> StatusEnum.fromValue("UNKNOWN"));
        assertThrows(IllegalArgumentException.class, () -> StatusEnum.fromValue(""));
        assertThrows(IllegalArgumentException.class, () -> StatusEnum.fromValue(null));
    }
}