package com.acmeinsurance.order.domain.model;

import com.acmeinsurance.order.enums.CategoryEnum;
import com.acmeinsurance.order.enums.PaymentMethodEnum;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import com.acmeinsurance.order.enums.SalesChannelEnum;
import com.acmeinsurance.order.enums.StatusEnum;
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
import java.util.Objects;
import java.util.UUID;

import static com.acmeinsurance.order.enums.StatusEnum.WAITING;

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
    private List<StatusHistoryEntry> history;
    private String paymentConfirmed;
    private String subscriptionAuthorized;

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
        List<StatusHistoryEntry> initialHistory = new ArrayList<>();
        initialHistory.add(StatusHistoryEntry.builder()
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
                .paymentConfirmed(WAITING.getValue())
                .subscriptionAuthorized(WAITING.getValue())
                .build();
    }

    public void updateStatus(final PolicyStatusEnum newStatus) {
        if (Objects.isNull(history)) {
            this.history = new ArrayList<>();
        }
        this.history.add(StatusHistoryEntry.builder()
                .status(newStatus)
                .timestamp(Instant.now())
                .build());
        this.status = newStatus;
    }

    public void markPaymentConfirmed(final boolean confirmed) {
        this.paymentConfirmed = StatusEnum.fromValue(confirmed).getValue();
    }

    public void markSubscriptionAuthorized(final boolean authorized) {
        this.subscriptionAuthorized = StatusEnum.fromValue(authorized).getValue();
    }
}