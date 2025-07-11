package com.acmeinsurance.order.domain.model;

import com.acmeinsurance.order.enums.PolicyStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistoryEntry {

    private PolicyStatusEnum status;
    private Instant timestamp;

}