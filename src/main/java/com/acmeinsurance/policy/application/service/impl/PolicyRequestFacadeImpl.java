package com.acmeinsurance.policy.application.service.impl;

import com.acmeinsurance.policy.application.dto.request.PolicyRequestDTO;
import com.acmeinsurance.policy.application.dto.response.PolicyRequestResponseDTO;
import com.acmeinsurance.policy.application.dto.response.PolicyResponseDTO;
import com.acmeinsurance.policy.application.mapper.PolicyRequestMapper;
import com.acmeinsurance.policy.application.service.PolicyRequestFacade;
import com.acmeinsurance.policy.domain.enums.CategoryEnum;
import com.acmeinsurance.policy.domain.enums.PaymentMethodEnum;
import com.acmeinsurance.policy.domain.enums.PolicyStatusEnum;
import com.acmeinsurance.policy.domain.enums.SalesChannelEnum;
import com.acmeinsurance.policy.domain.model.PolicyRequest;
import com.acmeinsurance.policy.domain.model.StatusHistoryEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PolicyRequestFacadeImpl implements PolicyRequestFacade {

    private final PolicyRequestMapper policyRequestMapper;


    @Override
    public Mono<PolicyRequestResponseDTO> createPolicyRequest(final PolicyRequestDTO requestDTO) {

        PolicyRequest mockDomainPolicyRequest = PolicyRequest.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .customerId(requestDTO.getCustomerId())
                .productId(requestDTO.getProductId())
                .category(requestDTO.getCategory())
                .salesChannel(requestDTO.getSalesChannel())
                .paymentMethod(requestDTO.getPaymentMethod())
                .totalMonthlyPremiumAmount(requestDTO.getTotalMonthlyPremiumAmount())
                .insuredAmount(requestDTO.getInsuredAmount())
                .coverages(requestDTO.getCoverages())
                .assistances(requestDTO.getAssistances())
                .status(PolicyStatusEnum.RECEIVED)
                .history(List.of(StatusHistoryEntry.builder().status(PolicyStatusEnum.RECEIVED).timestamp(Instant.now()).build()))
                .build();


        return Mono.just(mockDomainPolicyRequest)
                .map(policyRequestMapper::toCreationResponseDTO);
    }

    @Override
    public Mono<PolicyResponseDTO> getPolicyRequestById(final String id) {

        if ("non-existent-id".equals(id)) {
            return Mono.empty();
        }

        PolicyRequest mockDomainPolicyRequest = PolicyRequest.builder()
                .id(UUID.fromString(id))
                .customerId(UUID.randomUUID())
                .productId(1L)
                .category(CategoryEnum.AUTO)
                .salesChannel(SalesChannelEnum.MOBILE)
                .paymentMethod(PaymentMethodEnum.CREDIT_CARD)
                .totalMonthlyPremiumAmount(new BigDecimal("100.00"))
                .insuredAmount(new BigDecimal("200000.00"))
                .coverages(Map.of("Responsabilidade Civil", new BigDecimal("50000.00")))
                .assistances(List.of("Reboque 24h"))
                .createdAt(Instant.now().minusSeconds(86400))
                .status(PolicyStatusEnum.RECEIVED)
                .history(List.of(StatusHistoryEntry.builder().status(PolicyStatusEnum.RECEIVED).timestamp(Instant.now().minusSeconds(86400)).build()))
                .build();

        return Mono.just(mockDomainPolicyRequest)
                .map(policyRequestMapper::toResponseDTO);
    }

    @Override
    public Flux<PolicyResponseDTO> getPolicyRequestsByCustomerId(final String customerId) {

        if ("non-existent-customer-id".equals(customerId)) {
            return Flux.empty();
        }

        PolicyRequest mockPolicy1 = PolicyRequest.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.fromString(customerId))
                .productId(1L)
                .category(CategoryEnum.AUTO)
                .salesChannel(SalesChannelEnum.MOBILE)
                .paymentMethod(PaymentMethodEnum.CREDIT_CARD)
                .totalMonthlyPremiumAmount(new BigDecimal("150.00"))
                .insuredAmount(new BigDecimal("300000.00"))
                .coverages(Map.of("Roubo", new BigDecimal("250000.00")))
                .assistances(List.of("Carro Reserva"))
                .createdAt(Instant.now().minusSeconds(172800))
                .status(PolicyStatusEnum.APPROVED)
                .history(List.of(StatusHistoryEntry.builder().status(PolicyStatusEnum.RECEIVED).timestamp(Instant.now().minusSeconds(172800)).build(),
                                 StatusHistoryEntry.builder().status(PolicyStatusEnum.APPROVED).timestamp(Instant.now().minusSeconds(86400)).build()))
                .build();

        PolicyRequest mockPolicy2 = PolicyRequest.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.fromString(customerId))
                .productId(1L)
                .category(CategoryEnum.VIDA)
                .salesChannel(SalesChannelEnum.WEBSITE)
                .paymentMethod(PaymentMethodEnum.PIX)
                .totalMonthlyPremiumAmount(new BigDecimal("50.00"))
                .insuredAmount(new BigDecimal("100000.00"))
                .coverages(Map.of("Morte Acidental", new BigDecimal("100000.00")))
                .assistances(List.of("Assistência Funeral"))
                .createdAt(Instant.now().minusSeconds(259200))
                .status(PolicyStatusEnum.PENDING)
                .history(List.of(StatusHistoryEntry.builder().status(PolicyStatusEnum.RECEIVED).timestamp(Instant.now().minusSeconds(259200)).build(),
                                 StatusHistoryEntry.builder().status(PolicyStatusEnum.PENDING).timestamp(Instant.now().minusSeconds(172800)).build()))
                .build();

        return Flux.just(mockPolicy1, mockPolicy2)
                .map(policyRequestMapper::toResponseDTO);
    }
}