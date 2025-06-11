package com.acmeinsurance.order.application.controller;

import com.acmeinsurance.order.application.dto.policy.request.PolicyRequestDTO;
import com.acmeinsurance.order.application.dto.policy.response.PolicyRequestResponseDTO;
import com.acmeinsurance.order.application.dto.policy.response.PolicyResponseDTO;
import com.acmeinsurance.order.application.service.PolicyRequestFacade;
import com.acmeinsurance.order.enums.CategoryEnum;
import com.acmeinsurance.order.enums.PaymentMethodEnum;
import com.acmeinsurance.order.enums.SalesChannelEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PolicyRequestFacade policyRequestFacade;

    private PolicyRequestDTO createValidRequestDTO() {
        return PolicyRequestDTO.builder()
                .customerId(UUID.randomUUID())
                .productId(1L)
                .category(CategoryEnum.AUTO)
                .salesChannel(SalesChannelEnum.MOBILE)
                .paymentMethod(PaymentMethodEnum.CREDIT_CARD)
                .totalMonthlyPremiumAmount(new BigDecimal("75.25"))
                .insuredAmount(new BigDecimal("275000.50"))
                .coverages(Map.of("Roubo", new BigDecimal("100000.25")))
                .assistances(List.of("Guincho até 250km"))
                .build();
    }

    private PolicyRequestResponseDTO createValidRequestResponseDTO(final UUID id, final Instant createdAt) {
        return PolicyRequestResponseDTO.builder()
                .id(id)
                .createdAt(createdAt)
                .build();
    }

    private PolicyResponseDTO createValidPolicyResponseDTO(final String id, final UUID customerId, final Instant createdAt) {
        return PolicyResponseDTO.builder()
                .id(UUID.fromString(id))
                .customerId(customerId)
                .productId(1L)
                .category(CategoryEnum.AUTO)
                .salesChannel(SalesChannelEnum.MOBILE)
                .paymentMethod(PaymentMethodEnum.CREDIT_CARD)
                .totalMonthlyPremiumAmount(new BigDecimal("75.25"))
                .insuredAmount(new BigDecimal("275000.50"))
                .coverages(Map.of("Roubo", new BigDecimal("100000.25")))
                .assistances(List.of("Guincho até 250km"))
                .createdAt(createdAt)
                .build();
    }

    @Test
    @DisplayName("Should create a new policy request and return 201 Created")
    void shouldCreatePolicyRequest() {
        final UUID requestId = UUID.randomUUID();
        final Instant createdAt = Instant.now();
        final PolicyRequestResponseDTO expectedResponse = createValidRequestResponseDTO(requestId, createdAt);

        when(policyRequestFacade.createPolicyRequest(any(PolicyRequestDTO.class)))
                .thenReturn(Mono.just(expectedResponse));

        final PolicyRequestDTO request = createValidRequestDTO();

        webTestClient.post().uri("/api/solicitations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PolicyRequestResponseDTO.class)
                .value(response -> {
                    assertEquals(expectedResponse.getId(), response.getId());
                    assertEquals(expectedResponse.getCreatedAt().getEpochSecond(), response.getCreatedAt().getEpochSecond());
                });
    }

    @Test
    @DisplayName("Should return 400 Bad Request when mandatory fields are missing")
    void shouldReturnBadRequestWhenFieldsAreMissing() {
        final PolicyRequestDTO request = createValidRequestDTO();
        request.setCustomerId(null);

        webTestClient.post().uri("/api/solicitations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").exists();
    }

    @Test
    @DisplayName("Should return policy request by ID and 200 OK")
    void shouldReturnPolicyRequestById() {
        final String policyId = UUID.randomUUID().toString();
        final UUID customerId = UUID.randomUUID();
        final Instant createdAt = Instant.now();
        final PolicyResponseDTO expectedResponse = createValidPolicyResponseDTO(policyId, customerId, createdAt);

        when(policyRequestFacade.getPolicyRequestById(eq(policyId)))
                .thenReturn(Mono.just(expectedResponse));

        webTestClient.get().uri("/api/solicitations/{id}", policyId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PolicyResponseDTO.class)
                .value(response -> assertEquals(expectedResponse.getId(), response.getId()));
    }

    @Test
    @DisplayName("Should return 404 Not Found when policy request by ID does not exist")
    void shouldReturnNotFoundWhenPolicyRequestByIdDoesNotExist() {
        final String nonExistentId = UUID.randomUUID().toString();

        when(policyRequestFacade.getPolicyRequestById(eq(nonExistentId)))
                .thenReturn(Mono.empty());

        webTestClient.get().uri("/api/solicitations/{id}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Should return empty list and 200 OK when no policy requests found for customer ID")
    void shouldReturnEmptyListWhenNoPolicyRequestsFoundForCustomerId() {
        final UUID customerId = UUID.randomUUID();

        when(policyRequestFacade.getPolicyRequestsByCustomerId(Mockito.any()))
                .thenReturn(Flux.empty());

        webTestClient.get().uri("/api/solicitations/customer/{customerId}", customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PolicyResponseDTO.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("Should delete a policy request by ID and return 200 OK")
    void shouldDeletePolicyRequestById() {
        final String policyId = UUID.randomUUID().toString();
        final UUID customerId = UUID.randomUUID();
        final Instant createdAt = Instant.now();
        final PolicyResponseDTO deletedPolicy = createValidPolicyResponseDTO(policyId, customerId, createdAt);

        when(policyRequestFacade.deletePolicyRequestById(eq(policyId)))
                .thenReturn(Mono.just(deletedPolicy));

        webTestClient.delete().uri("/api/solicitations/{id}", policyId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PolicyResponseDTO.class)
                .value(response -> assertEquals(policyId, response.getId().toString())); // Convert UUID to String for comparison
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting a non-existent policy request")
    void shouldReturnNotFoundWhenDeletingNonExistentPolicyRequest() {
        final String nonExistentId = UUID.randomUUID().toString();

        when(policyRequestFacade.deletePolicyRequestById(eq(nonExistentId)))
                .thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/solicitations/{id}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound();
    }
}
