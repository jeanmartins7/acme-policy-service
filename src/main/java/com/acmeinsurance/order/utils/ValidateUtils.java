package com.acmeinsurance.order.utils;

import com.acmeinsurance.order.enums.PolicyStatusEnum;
import com.acmeinsurance.order.enums.StatusEnum;

public class ValidateUtils {

    private ValidateUtils() {}

    public static boolean isDenied(final String status){
        return StatusEnum.DENIED.equals(StatusEnum.fromValue(status));
    }

    public static boolean isRejected(final String status){
        return PolicyStatusEnum.REJECTED.equals(PolicyStatusEnum.fromValue(status));
    }

    public static boolean isConfirmed(final String status){
        return StatusEnum.CONFIRMED.equals(StatusEnum.fromValue(status));
    }

    public static boolean isInactived(final String status){
        return StatusEnum.INACTIVE.equals(StatusEnum.fromValue(status));
    }

    public static boolean isActived(final String status){
        return StatusEnum.ACTIVE.equals(StatusEnum.fromValue(status));
    }
}
