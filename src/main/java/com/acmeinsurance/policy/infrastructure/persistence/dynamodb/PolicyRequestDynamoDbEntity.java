package com.acmeinsurance.policy.infrastructure.persistence.dynamodb;

import com.acmeinsurance.policy.domain.enums.CategoryEnum;
import com.acmeinsurance.policy.domain.enums.PaymentMethodEnum;
import com.acmeinsurance.policy.domain.enums.PolicyStatusEnum;
import com.acmeinsurance.policy.domain.enums.SalesChannelEnum;
import com.acmeinsurance.policy.util.impl.EnumValueConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class PolicyRequestDynamoDbEntity {


    private String id;
    private String customerId;
    private String productId;

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

    private List<StatusHistoryEntryEntity> history;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "customer_id-index")
    public String getCustomerId() {
        return customerId;
    }

    @DynamoDbConvertedBy(EnumValueConverter.class)
    public CategoryEnum getCategory() {
        return category;
    }

    @DynamoDbConvertedBy(EnumValueConverter.class)
    public SalesChannelEnum getSalesChannel() {
        return salesChannel;
    }

    @DynamoDbConvertedBy(EnumValueConverter.class)
    public PaymentMethodEnum getPaymentMethod() {
        return paymentMethod;
    }

    @DynamoDbConvertedBy(EnumValueConverter.class)
    public PolicyStatusEnum getStatus() {
        return status;
    }


}
