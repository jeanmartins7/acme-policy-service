package com.acmeinsurance.order.utils;

import com.acmeinsurance.order.enums.StatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidateUtilsTest {

    @Test
    void isDenied_shouldReturnTrueForDenied() {
        assertTrue(ValidateUtils.isDenied(StatusEnum.DENIED.getValue()));
    }

    @Test
    void isDenied_shouldReturnFalseForOtherStatuses() {
        assertFalse(ValidateUtils.isDenied(StatusEnum.CONFIRMED.getValue()));
        assertFalse(ValidateUtils.isDenied(StatusEnum.ACTIVE.getValue()));
        assertFalse(ValidateUtils.isDenied(StatusEnum.INACTIVE.getValue()));
    }

    @Test
    void isConfirmed_shouldReturnTrueForConfirmed() {
        assertTrue(ValidateUtils.isConfirmed(StatusEnum.CONFIRMED.getValue()));
    }

    @Test
    void isConfirmed_shouldReturnFalseForOtherStatuses() {
        assertFalse(ValidateUtils.isConfirmed(StatusEnum.DENIED.getValue()));
        assertFalse(ValidateUtils.isConfirmed(StatusEnum.ACTIVE.getValue()));
        assertFalse(ValidateUtils.isConfirmed(StatusEnum.INACTIVE.getValue()));
    }

    @Test
    void isInactived_shouldReturnTrueForInactive() {
        assertTrue(ValidateUtils.isInactived(StatusEnum.INACTIVE.getValue()));
    }

    @Test
    void isInactived_shouldReturnFalseForOtherStatuses() {
        assertFalse(ValidateUtils.isInactived(StatusEnum.DENIED.getValue()));
        assertFalse(ValidateUtils.isInactived(StatusEnum.CONFIRMED.getValue()));
        assertFalse(ValidateUtils.isInactived(StatusEnum.ACTIVE.getValue()));
    }

    @Test
    void isActived_shouldReturnTrueForActive() {
        assertTrue(ValidateUtils.isActived(StatusEnum.ACTIVE.getValue()));
    }

    @Test
    void isActived_shouldReturnFalseForOtherStatuses() {
        assertFalse(ValidateUtils.isActived(StatusEnum.DENIED.getValue()));
        assertFalse(ValidateUtils.isActived(StatusEnum.CONFIRMED.getValue()));
        assertFalse(ValidateUtils.isActived(StatusEnum.INACTIVE.getValue()));
    }


    @Test
    void allMethods_shouldHandleInvalidStatusGracefully() {
        final String invalid = "UNKNOWN";
        assertThrows(IllegalArgumentException.class, () -> ValidateUtils.isConfirmed(invalid));
        assertThrows(IllegalArgumentException.class, () -> ValidateUtils.isInactived(invalid));
        assertThrows(IllegalArgumentException.class, () -> ValidateUtils.isActived(invalid));
    }
}