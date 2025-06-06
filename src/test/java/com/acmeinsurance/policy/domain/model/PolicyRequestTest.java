package com.acmeinsurance.policy.domain.model;

import com.acmeinsurance.policy.domain.enums.CategoryEnum;
import com.acmeinsurance.policy.domain.enums.PaymentMethodEnum;
import com.acmeinsurance.policy.domain.enums.PolicyStatusEnum;
import com.acmeinsurance.policy.domain.enums.SalesChannelEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyRequestTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final Long PRODUCT_ID = 1L;
    private static final CategoryEnum CATEGORY = CategoryEnum.AUTO;
    private static final SalesChannelEnum SALES_CHANNEL = SalesChannelEnum.MOBILE;
    private static final PaymentMethodEnum PAYMENT_METHOD = PaymentMethodEnum.CREDIT_CARD;
    private static final BigDecimal TOTAL_MONTHLY_PREMIUM = new BigDecimal("75.25");
    private static final BigDecimal INSURED_AMOUNT = new BigDecimal("275000.50");
    private static final Map<String, BigDecimal> COVERAGES = Map.of("Roubo", new BigDecimal("100000.25"));
    private static final List<String> ASSISTANCES = List.of("Guincho até 250km");

    @Test
    @DisplayName("Should create an initial policy request with RECEIVED status and history")
    void shouldCreateInitialPolicyRequest() {
        Instant beforeCreation = Instant.now().minusSeconds(1);

        PolicyRequest policyRequest = PolicyRequest.createInitialRequest(
                CUSTOMER_ID,
                PRODUCT_ID,
                CATEGORY,
                SALES_CHANNEL,
                PAYMENT_METHOD,
                TOTAL_MONTHLY_PREMIUM,
                INSURED_AMOUNT,
                COVERAGES,
                ASSISTANCES
        );

        assertNotNull(policyRequest.getId());
        assertEquals(CUSTOMER_ID, policyRequest.getCustomerId());
        assertEquals(PRODUCT_ID, policyRequest.getProductId());
        assertEquals(CATEGORY, policyRequest.getCategory());
        assertEquals(SALES_CHANNEL, policyRequest.getSalesChannel());
        assertEquals(PAYMENT_METHOD, policyRequest.getPaymentMethod());
        assertEquals(TOTAL_MONTHLY_PREMIUM, policyRequest.getTotalMonthlyPremiumAmount());
        assertEquals(INSURED_AMOUNT, policyRequest.getInsuredAmount());
        assertEquals(COVERAGES, policyRequest.getCoverages());
        assertEquals(ASSISTANCES, policyRequest.getAssistances());
        assertNotNull(policyRequest.getCreatedAt());
        assertTrue(policyRequest.getCreatedAt().isAfter(beforeCreation));
        assertEquals(PolicyStatusEnum.RECEIVED, policyRequest.getStatus());

        assertNotNull(policyRequest.getHistory());
        assertEquals(1, policyRequest.getHistory().size());

        final StatusHistoryEntry firstEntry = policyRequest.getHistory().get(0);
        assertEquals(PolicyStatusEnum.RECEIVED, firstEntry.getStatus());
        assertNotNull(firstEntry.getTimestamp());
        assertTrue(firstEntry.getTimestamp().isAfter(beforeCreation));
    }

    @Test
    @DisplayName("Should update policy status and add new entry to history")
    void shouldUpdatePolicyStatusAndAddHistory() {
        final PolicyRequest policyRequest = PolicyRequest.createInitialRequest(
                CUSTOMER_ID, PRODUCT_ID, CATEGORY, SALES_CHANNEL, PAYMENT_METHOD,
                TOTAL_MONTHLY_PREMIUM, INSURED_AMOUNT, COVERAGES, ASSISTANCES
        );

        final int initialHistorySize = policyRequest.getHistory().size();
        final Instant beforeUpdate = Instant.now().minusSeconds(1);
        final PolicyStatusEnum newStatus = PolicyStatusEnum.VALIDATED;

        policyRequest.updateStatus(newStatus);

        assertEquals(newStatus, policyRequest.getStatus());
        assertEquals(initialHistorySize + 1, policyRequest.getHistory().size());

        final StatusHistoryEntry newEntry = policyRequest.getHistory().get(policyRequest.getHistory().size() - 1);
        assertEquals(newStatus, newEntry.getStatus());
        assertNotNull(newEntry.getTimestamp());
        assertTrue(newEntry.getTimestamp().isAfter(beforeUpdate));
    }

    @Test
    @DisplayName("Should handle updateStatus when history is initially null")
    void shouldHandleUpdateStatusWhenHistoryIsNull() {
        final PolicyRequest policyRequest = PolicyRequest.builder()
                .id(UUID.randomUUID())
                .customerId(CUSTOMER_ID)
                .createdAt(Instant.now())
                .status(PolicyStatusEnum.RECEIVED)
                .history(null)
                .build();

        final int initialHistorySize = 0;
        final PolicyStatusEnum newStatus = PolicyStatusEnum.PENDING;

        policyRequest.updateStatus(newStatus);

        assertEquals(newStatus, policyRequest.getStatus());
        assertNotNull(policyRequest.getHistory());
        assertEquals(initialHistorySize + 1, policyRequest.getHistory().size());

        final StatusHistoryEntry newEntry = policyRequest.getHistory().get(0);
        assertEquals(newStatus, newEntry.getStatus());
    }
}