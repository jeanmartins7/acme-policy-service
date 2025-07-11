package com.acmeinsurance.order.infrastructure.persistence.entity;

import com.acmeinsurance.order.enums.PolicyStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class StatusHistoryEntryEntity {

    private PolicyStatusEnum status;

    private Instant timestamp;
}