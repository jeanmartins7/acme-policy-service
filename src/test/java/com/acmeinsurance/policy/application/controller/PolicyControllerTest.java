package com.acmeinsurance.policy.application.controller;

import com.acmeinsurance.policy.domain.enums.CategoryEnum;
import com.acmeinsurance.policy.domain.enums.PaymentMethodEnum;
import com.acmeinsurance.policy.domain.enums.SalesChannelEnum;
import com.acmeinsurance.policy.application.dto.request.PolicyRequestDTO;
import com.acmeinsurance.policy.application.dto.response.PolicyRequestResponseDTO;
import com.acmeinsurance.policy.application.service.PolicyRequestFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

    private PolicyRequestResponseDTO createValidResponseDTO(final UUID id, final Instant createdAt) {
        return PolicyRequestResponseDTO.builder()
                .id(id)
                .createdAt(createdAt)
                .build();
    }

    @Test
    @DisplayName("Should create a new policy request and return 201 Created")
    void shouldCreatePolicyRequest() {
        final UUID requestId = UUID.randomUUID();
        final Instant createdAt = Instant.now();
        final PolicyRequestResponseDTO expectedResponse = createValidResponseDTO(requestId, createdAt);

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
}