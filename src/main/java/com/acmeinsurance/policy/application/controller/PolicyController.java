package com.acmeinsurance.policy.application.controller;

import com.acmeinsurance.policy.application.dto.request.PolicyRequest;
import com.acmeinsurance.policy.application.dto.response.PolicyResponse;
import com.acmeinsurance.policy.application.service.PolicyFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solicitations")
public class PolicyController {

    private final PolicyFacade policyFacade;

    @PostMapping
    public Mono<ResponseEntity<PolicyResponse>> createPolicyRequest(
            @Valid @RequestBody final PolicyRequest policyRequest) {

        return policyFacade.createPolicyRequest(policyRequest)
                .map(responseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(responseDTO));
    }
}