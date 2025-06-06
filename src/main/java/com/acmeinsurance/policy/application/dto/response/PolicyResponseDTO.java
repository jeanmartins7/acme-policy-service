package com.acmeinsurance.policy.application.dto.response;

import com.acmeinsurance.policy.domain.enums.CategoryEnum;
import com.acmeinsurance.policy.domain.enums.PaymentMethodEnum;
import com.acmeinsurance.policy.domain.enums.PolicyStatusEnum;
import com.acmeinsurance.policy.domain.enums.SalesChannelEnum;
import com.acmeinsurance.policy.domain.model.StatusHistoryEntry;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PolicyResponseDTO {

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
}