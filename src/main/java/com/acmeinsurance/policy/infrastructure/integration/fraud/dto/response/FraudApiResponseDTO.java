package com.acmeinsurance.policy.infrastructure.integration.fraud.dto.response;

import com.acmeinsurance.policy.enums.ClassificationEnum;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FraudApiResponseDTO {

    private UUID orderId;

    private UUID customerId;

    private Instant analyzedAt;

    private ClassificationEnum classification;

    private List<OccurrenceDTO> occurrences;
}

