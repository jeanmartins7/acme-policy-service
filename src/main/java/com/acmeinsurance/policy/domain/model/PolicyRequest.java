package com.acmeinsurance.policy.domain.model;

import com.acmeinsurance.policy.domain.enums.CategoryEnum;
import com.acmeinsurance.policy.domain.enums.PaymentMethodEnum;
import com.acmeinsurance.policy.domain.enums.PolicyStatusEnum;
import com.acmeinsurance.policy.domain.enums.SalesChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRequest {

    private UUID id;
    private UUID customerId;
    private Long productId;
    private CategoryEnum category;
    private SalesChannelEnum salesChannel;
    private PaymentMethodEnum paymentMethod;
    private BigDecimal totalMonthlyPremiumAmount;
    private BigDecimal insuredAmount;
    private Map<String, BigDecimal> coverages;
    private List<String> assistances;
    private Instant createdAt;
    private Instant finishedAt;
    private PolicyStatusEnum status;
    private List<StatusHistory> history;

    public static PolicyRequest createInitialRequest(
            UUID customerId,
            Long productId,
            CategoryEnum category,
            SalesChannelEnum salesChannel,
            PaymentMethodEnum paymentMethod,
            BigDecimal totalMonthlyPremiumAmount,
            BigDecimal insuredAmount,
            Map<String, BigDecimal> coverages,
            List<String> assistances) {

        Instant now = Instant.now();
        final PolicyStatusEnum initialStatus = PolicyStatusEnum.RECEIVED;
        List<StatusHistory> initialHistory = new ArrayList<>();
        initialHistory.add(StatusHistory.builder()
                .status(initialStatus)
                .timestamp(now)
                .build());

        return PolicyRequest.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .productId(productId)
                .category(category)
                .salesChannel(salesChannel)
                .paymentMethod(paymentMethod)
                .totalMonthlyPremiumAmount(totalMonthlyPremiumAmount)
                .insuredAmount(insuredAmount)
                .coverages(coverages)
                .assistances(assistances)
                .createdAt(now)
                .status(initialStatus)
                .history(initialHistory)
                .build();
    }

    public void updateStatus(final PolicyStatusEnum newStatus) {
        if (this.history == null) {
            this.history = new ArrayList<>();
        }
        this.history.add(StatusHistory.builder()
                .status(newStatus)
                .timestamp(Instant.now())
                .build());
        this.status = newStatus;
    }
}