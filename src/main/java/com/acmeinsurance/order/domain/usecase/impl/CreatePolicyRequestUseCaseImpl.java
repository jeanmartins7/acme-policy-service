package com.acmeinsurance.order.domain.usecase.impl;

import com.acmeinsurance.order.avro.PolicyReceivedEvent;
import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
import com.acmeinsurance.order.domain.usecase.CreatePolicyRequestUseCase;
import com.acmeinsurance.order.domain.usecase.command.CreatePolicyRequestCommand;
import com.acmeinsurance.order.infrastructure.kafka.producer.PolicyEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Service
@RequiredArgsConstructor
public class CreatePolicyRequestUseCaseImpl implements CreatePolicyRequestUseCase {



    private static final Logger log = LoggerFactory.getLogger(CreatePolicyRequestUseCaseImpl.class); // NOVO: Logger


    private static final String POLICY_RECEIVED = "RECEIVED";
    private final PolicyRequestRepository policyRequestRepository;
    private final PolicyEventPublisher policyEventPublisher;

    @Override
    public Mono<PolicyRequest> execute(final CreatePolicyRequestCommand command) {

        final PolicyRequest newPolicyRequest = PolicyRequest.createInitialRequest(
                command.getCustomerId(),
                command.getProductId(),
                command.getCategory(),
                command.getSalesChannel(),
                command.getPaymentMethod(),
                command.getTotalMonthlyPremiumAmount(),
                command.getInsuredAmount(),
                command.getCoverages(),
                command.getAssistances()
        );

        return policyRequestRepository.save(newPolicyRequest)
                .flatMap(this::sendPolicyRequest);
    }

    private Mono<PolicyRequest> sendPolicyRequest(final PolicyRequest savedPolicy) {

        final PolicyReceivedEvent avroEvent = PolicyReceivedEvent.newBuilder()
                .setPolicyId(savedPolicy.getId().toString())
                .setCustomerId(savedPolicy.getCustomerId().toString())
                .setProductId(savedPolicy.getProductId())
                .setCategory(savedPolicy.getCategory().getValue())
                .setSalesChannel(savedPolicy.getSalesChannel().getValue())
                .setPaymentMethod(savedPolicy.getPaymentMethod().getValue())
                .setTotalMonthlyPremiumAmount(savedPolicy.getTotalMonthlyPremiumAmount())
                .setInsuredAmount(savedPolicy.getInsuredAmount())
                .setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .build();

        try {
            return policyEventPublisher.publishPolicyReceivedEvent(avroEvent, POLICY_RECEIVED)
                    .thenReturn(savedPolicy);
        } catch (Exception e) {
            // LOG DETALHADO E printStackTrace() para forçar a saída do erro
            log.error("CRITICAL ERROR IN AVRO PUBLISHING: Failed to build or publish Avro PolicyReceivedEvent for policyId {}. See stack trace below.", savedPolicy.getId(), e);
            System.err.println("\n--- RAW EXCEPTION STACK TRACE (CreatePolicyRequestUseCase) ---");
            e.printStackTrace(System.err); // Força a impressão no System.err
            System.err.println("--- END RAW EXCEPTION STACK TRACE ---\n");

            return Mono.error(new RuntimeException("Avro serialization/publishing failed for policyId " + savedPolicy.getId(), e));
        }
    }
}
