package com.acmeinsurance.order.application.controller;

import com.acmeinsurance.order.application.dto.policy.request.PolicyRequestDTO;
import com.acmeinsurance.order.application.dto.policy.response.PolicyRequestResponseDTO;
import com.acmeinsurance.order.application.dto.policy.response.PolicyResponseDTO;
import com.acmeinsurance.order.application.service.PolicyRequestFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solicitations")
public class PolicyController {

    private final PolicyRequestFacade policyRequestFacade;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<PolicyResponseDTO>> getPolicyRequestById(@PathVariable final String id) {
        return policyRequestFacade.getPolicyRequestById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public Flux<PolicyResponseDTO> getPolicyRequestsByCustomerId(@PathVariable final String customerId) {
        return policyRequestFacade.getPolicyRequestsByCustomerId(customerId);
    }

    @PostMapping
    public Mono<ResponseEntity<PolicyRequestResponseDTO>> createPolicyRequest(
            @Valid @RequestBody final PolicyRequestDTO policyRequestDTO) {

        return policyRequestFacade.createPolicyRequest(policyRequestDTO)
                .map(responseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(responseDTO));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<PolicyResponseDTO>> deletePolicyRequestById(@PathVariable final String id) {
        return policyRequestFacade.deletePolicyRequestById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}