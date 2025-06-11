package com.acmeinsurance.order.handler;

import com.acmeinsurance.order.enums.PolicyStatusEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class PolicyCancelledException extends RuntimeException {

    public PolicyCancelledException(final String policyId, final PolicyStatusEnum policyStatus) {
        super("Policy " + policyId + " can't be CANCELLED status, policyStatus is " + policyStatus.getValue() +".");
    }
}