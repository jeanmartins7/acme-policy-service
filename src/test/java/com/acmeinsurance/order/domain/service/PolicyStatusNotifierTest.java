//package com.acmeinsurance.order.domain.service;
//
//import com.acmeinsurance.order.avro.PolicyStatusChangedEvent;
//import com.acmeinsurance.order.domain.model.PolicyRequest;
//import com.acmeinsurance.order.enums.PolicyStatusEnum;
//import com.acmeinsurance.order.infrastructure.kafka.producer.PolicyEventPublisher;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.util.UUID;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//
//@ExtendWith(SpringExtension.class)
//class PolicyStatusNotifierTest {
//
//    @InjectMocks
//    private PolicyStatusNotifier notifier;
//
//    private PolicyEventPublisher listener;
//
//    @BeforeEach
//    void setUp() {
//        listener = mock(PolicyEventPublisher.class);
//    }
//
//    @Test
//    void notifyStatusChange_shouldCallListener() {
//        final PolicyStatusChangedEvent policyRequest = mock(PolicyStatusChangedEvent.class);
//        final PolicyRequest policyRequest1 = mock(PolicyRequest.class);
//        policyRequest1.setId(UUID.randomUUID());
//
//        notifier.notifyPolicyStatusChanged(policyRequest1, PolicyStatusEnum.RECEIVED, PolicyStatusEnum.VALIDATED);
//
//        verify(listener).publishEventNotificationStatus(policyRequest, "NOTIFICATION_STATUS");
//    }
//}