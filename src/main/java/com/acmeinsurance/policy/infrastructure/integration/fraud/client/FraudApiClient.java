package com.acmeinsurance.policy.infrastructure.integration.fraud.client;

import com.acmeinsurance.policy.infrastructure.integration.fraud.dto.response.FraudApiResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "fraud-api", url = "${fraud.api.url}")
public interface FraudApiClient {

    @GetMapping("/api/fraudes/solicitacoes/{orderId}/clientes/{customerId}")
    FraudApiResponseDTO getFraudClassification(
            @PathVariable("orderId") final UUID orderId,
            @PathVariable("customerId") final UUID customerId);
}

