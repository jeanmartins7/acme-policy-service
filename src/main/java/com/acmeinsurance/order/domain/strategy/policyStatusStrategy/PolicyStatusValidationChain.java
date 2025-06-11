package com.acmeinsurance.order.domain.strategy.policyStatusStrategy;

import com.acmeinsurance.order.avro.PaymentProcessedEvent;
import com.acmeinsurance.order.avro.SubscriptionProcessedEvent;
import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.enums.PolicyStatusEnum;
import com.acmeinsurance.order.handler.InvalidPolicyStatusException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PolicyStatusValidationChain {

    private final List<PolicyStatusValidationStrategy> strategies;
    private Map<PolicyStatusEnum, PolicyStatusValidationStrategy> strategyMap;

    public PolicyStatusValidationChain(final List<PolicyStatusValidationStrategy> strategies) {
        this.strategies = strategies;
    }

    @PostConstruct
    public void init() {
        strategyMap = strategies.stream()
                .collect(Collectors.toMap(PolicyStatusValidationStrategy::getSupportedStatus, Function.identity()));
    }

    private Mono<PolicyRequest> applyValidation(
            final PolicyRequest policyRequest,
            final BiFunction<PolicyStatusValidationStrategy, PolicyRequest, Mono<PolicyRequest>> validationFunction) {

        return Optional.ofNullable(policyRequest)
                .map(request -> Optional.ofNullable(request.getStatus())
                        .map(status -> Optional.ofNullable(strategyMap.get(status))
                                .map(strategy -> validationFunction.apply(strategy, request))
                                .orElseGet(() -> Mono.error(new InvalidPolicyStatusException("No validation strategy found for status: " + status))))
                        .orElseGet(() -> Mono.error(new IllegalArgumentException("PolicyRequest status cannot be null for validation."))))
                .orElseGet(() -> Mono.error(new IllegalArgumentException("PolicyRequest cannot be null for validation.")));
    }

    private Mono<PolicyRequest> applyValidationWithEvent(
            final PolicyRequest policyRequest,
            final Object event,
            final TriFunction<PolicyStatusValidationStrategy, PolicyRequest, Object, Mono<PolicyRequest>> validationFunction) {

        return Optional.ofNullable(policyRequest)
                .map(request -> Optional.ofNullable(request.getStatus())
                        .map(status -> Optional.ofNullable(strategyMap.get(status))
                                .map(strategy -> validationFunction.apply(strategy, request, event))
                                .orElseGet(() -> Mono.error(new InvalidPolicyStatusException("No validation strategy found for status: " + status))))
                        .orElseGet(() -> Mono.error(new IllegalArgumentException("PolicyRequest status cannot be null for validation."))))
                .orElseGet(() -> Mono.error(new IllegalArgumentException("PolicyRequest cannot be null for validation.")));
    }

    public Mono<PolicyRequest> validateCancel(final PolicyRequest policyRequest) {
        return applyValidation(policyRequest, PolicyStatusValidationStrategy::validateCancel);
    }

    public void applyPending(final PolicyRequest policyRequest) {
        applyValidation(policyRequest, PolicyStatusValidationStrategy::applyPending).subscribe();
    }

    public Mono<PolicyRequest> applyPayment(final PolicyRequest policyRequest, final PaymentProcessedEvent paymentProcessedEvent) {
        return applyValidationWithEvent(policyRequest, paymentProcessedEvent, (strategy, request, event) ->
                strategy.applyPayment(request, (PaymentProcessedEvent) event));
    }

    public Mono<PolicyRequest> applySubscriptionAuthorized(final PolicyRequest policyRequest, final SubscriptionProcessedEvent subscriptionProcessedEvent) {
        return applyValidationWithEvent(policyRequest, subscriptionProcessedEvent, (strategy, request, event) ->
                strategy.applySubscriptionAuthorized(request, (SubscriptionProcessedEvent) event));
    }
}

@FunctionalInterface
interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
}