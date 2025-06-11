package com.acmeinsurance.order.infrastructure.integration.fraud.dto.model;

import com.acmeinsurance.order.enums.ClassificationEnum;
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
public class FraudAnalysisResult {
    private UUID orderId;
    private UUID customerId;
    private Instant analyzedAt;
    private ClassificationEnum classification;
    private List<Occurrence> occurrences;
}