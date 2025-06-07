package com.acmeinsurance.policy.infrastructure.integration.fraud.dto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Occurrence {

    private UUID id;

    private Long productId;

    private String type;

    private String description;

    private Instant createdAt;

    private Instant updatedAt;
}