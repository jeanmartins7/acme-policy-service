package com.acmeinsurance.policy.domain.model;

import com.acmeinsurance.policy.domain.enums.PolicyStatusEnum;
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
public class StatusHistory {

    private PolicyStatusEnum status;
    private Instant timestamp;

}