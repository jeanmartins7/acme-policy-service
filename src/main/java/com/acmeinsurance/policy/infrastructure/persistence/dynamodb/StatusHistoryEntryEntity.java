package com.acmeinsurance.policy.infrastructure.persistence.dynamodb;

import com.acmeinsurance.policy.domain.enums.PolicyStatusEnum;
import com.acmeinsurance.policy.util.impl.EnumValueConverter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StatusHistoryEntryEntity {

    private PolicyStatusEnum status;

    private Instant timestamp;

    @DynamoDbConvertedBy(EnumValueConverter.class)
    public PolicyStatusEnum getStatus() {
        return status;
    }
}