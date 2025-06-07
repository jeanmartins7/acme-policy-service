package com.acmeinsurance.policy.handler;

public class PolicyRequestNotFoundException extends RuntimeException {

    public PolicyRequestNotFoundException(final String message) {
        super(message);
    }
}