package com.acmeinsurance.order.domain.usecase.command;

import com.acmeinsurance.order.enums.CategoryEnum;
import com.acmeinsurance.order.enums.PaymentMethodEnum;
import com.acmeinsurance.order.enums.SalesChannelEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CreatePolicyRequestCommand {
    private UUID customerId;
    private Long productId;
    private CategoryEnum category;
    private SalesChannelEnum salesChannel;
    private PaymentMethodEnum paymentMethod;
    private BigDecimal totalMonthlyPremiumAmount;
    private BigDecimal insuredAmount;
    private Map<String, BigDecimal> coverages;
    private List<String> assistances;
}