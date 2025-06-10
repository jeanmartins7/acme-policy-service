package com.acmeinsurance.order.infrastructure.persistence.entity;

import com.acmeinsurance.order.infrastructure.persistence.converter.StatusHistoryEntryListConverter;
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

    private String category;
    private String salesChannel;
    private String paymentMethod;
    private String status;

    private BigDecimal totalMonthlyPremiumAmount;
    private BigDecimal insuredAmount;
    private Map<String, BigDecimal> coverages;
    private List<String> assistances;
    private Instant createdAt;
    private Instant finishedAt;

    private List<StatusHistoryEntryEntity> history;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "customer_id-index")
    public String getCustomerId() {
        return customerId;
    }

    @DynamoDbConvertedBy(StatusHistoryEntryListConverter.class)
    public List<StatusHistoryEntryEntity> getHistory() {
        return history;
    }
}
