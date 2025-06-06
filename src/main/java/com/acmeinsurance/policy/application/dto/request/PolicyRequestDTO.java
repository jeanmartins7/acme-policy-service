package com.acmeinsurance.policy.application.dto.request;

import com.acmeinsurance.policy.domain.enums.CategoryEnum;
import com.acmeinsurance.policy.domain.enums.PaymentMethodEnum;
import com.acmeinsurance.policy.domain.enums.SalesChannelEnum;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PolicyRequestDTO {

    @NotNull(message = "Customer ID cannot be null")
    private UUID customerId;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    private CategoryEnum category;

    @NotNull(message = "Sales Channel cannot be null")
    private SalesChannelEnum salesChannel;

    @NotNull(message = "Payment Method cannot be null")
    private PaymentMethodEnum paymentMethod;

    @NotNull(message = "Total Monthly Premium Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Total Monthly Premium Amount must be positive")
    private BigDecimal totalMonthlyPremiumAmount;

    @NotNull(message = "Insured Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Insured Amount must be positive")
    private BigDecimal insuredAmount;

    @NotNull(message = "Coverages cannot be null")
    private Map<String, BigDecimal> coverages;

    @NotNull(message = "Assistances cannot be null")
    private List<String> assistances;
}