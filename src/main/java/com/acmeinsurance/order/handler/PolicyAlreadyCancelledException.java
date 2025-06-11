package com.acmeinsurance.order.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class PolicyAlreadyCancelledException extends RuntimeException {

    public PolicyAlreadyCancelledException(String policyId) {
        super("Policy " + policyId + " is already in CANCELLED status.");
    }
}