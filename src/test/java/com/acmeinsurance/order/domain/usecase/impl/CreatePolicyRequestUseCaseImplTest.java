package com.acmeinsurance.order.domain.usecase.impl;

import com.acmeinsurance.order.avro.PolicyReceivedEvent;
import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.domain.usecase.command.CreatePolicyRequestCommand;
import com.acmeinsurance.order.enums.CategoryEnum;
import com.acmeinsurance.order.enums.PaymentMethodEnum;
import com.acmeinsurance.order.enums.SalesChannelEnum;
import com.acmeinsurance.order.infrastructure.kafka.producer.PolicyEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreatePolicyRequestUseCaseImplTest {

    private PolicyRequestRepository repository;
    private PolicyEventPublisher publisher;
    private CreatePolicyRequestUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        repository = mock(PolicyRequestRepository.class);
        publisher = mock(PolicyEventPublisher.class);
        useCase = new CreatePolicyRequestUseCaseImpl(repository, publisher);
    }

    @Test
    void execute_shouldSaveAndPublishPolicyRequest() {
        CreatePolicyRequestCommand command = mock(CreatePolicyRequestCommand.class);
        PolicyRequest policyRequest = mock(PolicyRequest.class);

        when(repository.save(any())).thenReturn(Mono.just(policyRequest));
        when(policyRequest.getId()).thenReturn(UUID.randomUUID());
        when(policyRequest.getCustomerId()).thenReturn(UUID.randomUUID());
        when(policyRequest.getProductId()).thenReturn(1L);
        when(policyRequest.getCategory()).thenReturn(CategoryEnum.OUTRO);
        when(policyRequest.getSalesChannel()).thenReturn(SalesChannelEnum.MOBILE);
        when(policyRequest.getPaymentMethod()).thenReturn(PaymentMethodEnum.BOLETO);
        when(policyRequest.getTotalMonthlyPremiumAmount()).thenReturn(BigDecimal.valueOf(100.0));
        when(policyRequest.getInsuredAmount()).thenReturn(BigDecimal.valueOf(1000.0));

        when(publisher.publishPolicyReceivedEvent(any(PolicyReceivedEvent.class), anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(command))
                .expectNext(policyRequest)
                .verifyComplete();

        verify(repository, times(1)).save(any());
        verify(publisher, times(1)).publishPolicyReceivedEvent(any(PolicyReceivedEvent.class), eq("RECEIVED"));
    }

    @Test
    void sendPolicyRequest_shouldReturnErrorOnPublisherException() {
        PolicyRequest policyRequest = mock(PolicyRequest.class);
        when(policyRequest.getId()).thenReturn(UUID.randomUUID());
        when(policyRequest.getCustomerId()).thenReturn(UUID.randomUUID());
        when(policyRequest.getProductId()).thenReturn(1L);
        when(policyRequest.getCategory()).thenReturn(CategoryEnum.OUTRO);
        when(policyRequest.getSalesChannel()).thenReturn(SalesChannelEnum.MOBILE);
        when(policyRequest.getPaymentMethod()).thenReturn(PaymentMethodEnum.BOLETO);
        when(policyRequest.getTotalMonthlyPremiumAmount()).thenReturn(BigDecimal.valueOf(100.0));
        when(policyRequest.getInsuredAmount()).thenReturn(BigDecimal.valueOf(1000.0));

        when(publisher.publishPolicyReceivedEvent(any(PolicyReceivedEvent.class), anyString()))
                .thenThrow(new RuntimeException("Kafka error"));

        when(repository.save(any())).thenReturn(Mono.just(policyRequest));

        CreatePolicyRequestCommand command = mock(CreatePolicyRequestCommand.class);

        StepVerifier.create(useCase.execute(command))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().contains("Avro serialization/publishing failed"))
                .verify();

        verify(repository, times(1)).save(any());
        verify(publisher, times(1)).publishPolicyReceivedEvent(any(PolicyReceivedEvent.class), eq("RECEIVED"));
    }
}