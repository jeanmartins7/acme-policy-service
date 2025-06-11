package com.acmeinsurance.order.infrastructure.integration.fraud.service;

import com.acmeinsurance.order.infrastructure.integration.fraud.client.FraudApiClient;
import com.acmeinsurance.order.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;
import com.acmeinsurance.order.infrastructure.integration.fraud.dto.model.Occurrence;
import com.acmeinsurance.order.infrastructure.integration.fraud.mapper.FraudApiMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class FraudApiService {

    private static final Logger log = LoggerFactory.getLogger(FraudApiService.class);

    private final FraudApiClient fraudApiClient;
    private final FraudApiMapper fraudApiMapper;

    public Mono<FraudAnalysisResult> analyzePolicyRequest(final UUID orderId, final UUID customerId) {
        log.info("Calling Fraud API via Feign Client for orderId: {} and customerId: {}", orderId, customerId);

        return Mono.fromCallable(() -> fraudApiMapper.toDomain(fraudApiClient.getFraudClassification(orderId, customerId)))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("Error calling Fraud API for orderId {}: {}", orderId, e.getMessage()))
                .onErrorResume(e -> Mono.just(createFallbackFraudResult(orderId, customerId, e)));
    }

    private FraudAnalysisResult createFallbackFraudResult(UUID orderId, UUID customerId, Throwable e) {
        return FraudAnalysisResult.builder()
                .orderId(orderId)
                .customerId(customerId)
                .analyzedAt(Instant.now())
                .occurrences(Collections.singletonList(Occurrence.builder()
                        .description("Fraud API unavailable or error: " + e.getMessage())
                        .build()))
                .build();
    }
}