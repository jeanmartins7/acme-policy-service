//package com.acmeinsurance.order.infrastructure.kafka;
//
//import com.acmeinsurance.order.Application;
//import com.acmeinsurance.order.avro.PaymentProcessedEvent;
//import com.acmeinsurance.order.avro.PolicyReceivedEvent;
//import com.acmeinsurance.order.domain.model.PolicyRequest;
//import com.acmeinsurance.order.domain.repository.PolicyRequestRepository;
//import com.acmeinsurance.order.enums.CategoryEnum;
//import com.acmeinsurance.order.enums.ClassificationEnum;
//import com.acmeinsurance.order.enums.PaymentMethodEnum;
//import com.acmeinsurance.order.enums.PolicyStatusEnum;
//import com.acmeinsurance.order.enums.SalesChannelEnum;
//import com.acmeinsurance.order.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;
//import com.acmeinsurance.order.infrastructure.integration.fraud.service.FraudApiService;
//import org.apache.kafka.clients.admin.AdminClient;
//import org.apache.kafka.clients.admin.NewTopic;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.SpyBean;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.KafkaContainer;
//import org.testcontainers.containers.Network;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.utility.DockerImageName;
//import reactor.core.publisher.Mono;
//import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
//import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.ZoneOffset;
//import java.time.format.DateTimeFormatter;
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.argThat;
//import static org.mockito.Mockito.eq;
//import static org.mockito.Mockito.reset;
//import static org.mockito.Mockito.timeout;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest(classes = Application.class)
//@ActiveProfiles("test")
//@Testcontainers
//public class PolicyReceivedEventListenerIntegrationTest {
//
//    private static Network network = Network.newNetwork();
//
//    @Container
//    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
//            .withNetwork(network)
//            .withNetworkAliases("kafka-broker");
//
//    @Container
//    public static GenericContainer<?> schemaRegistry = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-schema-registry:7.4.0"))
//            .withNetwork(network)
//            .withNetworkAliases("schema-registry")
//            .withExposedPorts(8081)
//            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
//            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
//            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka-broker:9092")
//            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_SECURITY_PROTOCOL", "PLAINTEXT")
//            .dependsOn(kafka);
//
//    @DynamicPropertySource
//    static void dynamicProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
//        registry.add("spring.kafka.properties.schema.registry.url", () -> {
//            return "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081);
//        });
//        registry.add("policy.kafka.topics.status-notifications", () -> "policy-status-notifications-topic-test");
//        registry.add("policy.kafka.topics.payment-events", () -> "payment-confirmed-events-topic-test");
//        registry.add("policy.kafka.topics.subscription-events", () -> "subscription-authorized-events-topic-test");
//        registry.add("policy.kafka.topics.dead-letter-queue", () -> "policy-dlq-topic-test");
//        registry.add("app.event.source-id", () -> "test-instance");
//    }
//
//    @Autowired
//    private KafkaTemplate<String, Object> kafkaTemplate;
//
//    @MockBean
//    private PolicyRequestRepository policyRequestRepository;
//
//    @SpyBean
//    private FraudApiService fraudApiService;
//
//    @MockBean
//    private DynamoDbAsyncClient dynamoDbAsyncClient;
//
//    @MockBean
//    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
//
//    private static AdminClient adminClient;
//
//    @BeforeAll
//    static void setupContainers() {
//        adminClient = AdminClient.create(Collections.singletonMap("bootstrap.servers", kafka.getBootstrapServers()));
//
//        List<String> topicNames = List.of(
//                "policy-status-notifications-topic-test",
//                "payment-confirmed-events-topic-test",
//                "subscription-authorized-events-topic-test",
//                "policy-dlq-topic-test"
//        );
//        try {
//            System.out.println("Deleting existing test topics...");
//            adminClient.deleteTopics(topicNames).all().get(30, TimeUnit.SECONDS);
//            System.out.println("Existing test topics deleted (or did not exist).");
//        } catch (Exception e) {
//            System.err.println("Failed to delete existing test topics (may not exist, which is fine): " + e.getMessage());
//        }
//
//        List<NewTopic> topics = List.of(
//                new NewTopic("policy-status-notifications-topic-test", 1, (short) 1),
//                new NewTopic("payment-confirmed-events-topic-test", 1, (short) 1),
//                new NewTopic("subscription-authorized-events-topic-test", 1, (short) 1),
//                new NewTopic("policy-dlq-topic-test", 1, (short) 1)
//        );
//        try {
//            adminClient.createTopics(topics).all().get(60, TimeUnit.SECONDS);
//            System.out.println("Test topics created successfully.");
//        } catch (InterruptedException | ExecutionException | java.util.concurrent.TimeoutException e) {
//            System.err.println("Failed to create test topics: " + e.getMessage());
//            e.printStackTrace();
//            throw new RuntimeException("Failed to create Kafka topics for test", e);
//        }
//    }
//
//    @BeforeEach
//    void setup() {
//        reset(fraudApiService);
//
//        when(fraudApiService.analyzePolicyRequest(any(UUID.class), any(UUID.class)))
//                .thenReturn(Mono.just(FraudAnalysisResult.builder()
//                        .orderId(UUID.randomUUID())
//                        .customerId(UUID.randomUUID())
//                        .analyzedAt(Instant.now())
//                        .classification(ClassificationEnum.REGULAR)
//                        .occurrences(Collections.emptyList())
//                        .build()));
//
//        when(policyRequestRepository.save(any(PolicyRequest.class))).thenAnswer(invocation -> {
//            PolicyRequest pr = invocation.getArgument(0);
//            return Mono.just(pr);
//        });
//    }
//
//    @Test
//    @DisplayName("should process PolicyReceivedEvent and update status to VALIDATED")
//    void shouldProcessPolicyReceivedEventAndUpdateStatusToValidated() throws Exception {
//        PolicyRequest initialPolicyRequest = PolicyRequest.createInitialRequest(
//                UUID.randomUUID(), 123L, CategoryEnum.AUTO, SalesChannelEnum.MOBILE,
//                PaymentMethodEnum.CREDIT_CARD, BigDecimal.valueOf(100.00), BigDecimal.valueOf(10000.00),
//                Collections.emptyMap(), Collections.emptyList()
//        );
//
//        PolicyReceivedEvent avroEvent = PolicyReceivedEvent.newBuilder()
//                .setPolicyId(initialPolicyRequest.getId().toString())
//                .setCustomerId(initialPolicyRequest.getCustomerId().toString())
//                .setProductId(initialPolicyRequest.getProductId())
//                .setCategory(initialPolicyRequest.getCategory().getValue())
//                .setSalesChannel(initialPolicyRequest.getSalesChannel().getValue())
//                .setPaymentMethod(initialPolicyRequest.getPaymentMethod().getValue())
//                .setTotalMonthlyPremiumAmount(initialPolicyRequest.getTotalMonthlyPremiumAmount())
//                .setInsuredAmount(initialPolicyRequest.getInsuredAmount())
//                .setCreatedAt(LocalDate.ofInstant(initialPolicyRequest.getCreatedAt(), ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE))
//                .build();
//
//        Message<PolicyReceivedEvent> policyReceivedMessage = MessageBuilder.withPayload(avroEvent)
//                .setHeader(KafkaHeaders.KEY, avroEvent.getPolicyId())
//                .setHeader("eventType", "POLICY_RECEIVED")
//                .setHeader("eventSourceId", "test-instance")
//                .build();
//
//        kafkaTemplate.send("subscription-authorized-events-topic-test", policyReceivedMessage).get(60, TimeUnit.SECONDS);
//
//        verify(fraudApiService, timeout(60000)).analyzePolicyRequest(eq(initialPolicyRequest.getId()), eq(initialPolicyRequest.getCustomerId()));
//        verify(policyRequestRepository, timeout(60000)).save(any(PolicyRequest.class));
//        verify(policyRequestRepository, timeout(60000)).save(argThat(policy ->
//                policy.getId().equals(initialPolicyRequest.getId()) &&
//                        policy.getStatus().equals(PolicyStatusEnum.VALIDATED)
//        ));
//
//        UUID paymentPolicyId = UUID.randomUUID();
//        String paymentTransactionId = "TRANS-" + UUID.randomUUID().toString();
//        BigDecimal paymentAmount = BigDecimal.valueOf(250.00);
//
//        PaymentProcessedEvent paymentEvent = PaymentProcessedEvent.newBuilder()
//                .setPolicyId(paymentPolicyId.toString())
//                .setPaymentTransactionId(paymentTransactionId)
//                .setStatus("CONFIRMED")
//                .setAmount(paymentAmount)
//                .setPaymentDate(LocalDate.ofInstant(Instant.now(), ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE))
//                .build();
//
//        Message<PaymentProcessedEvent> paymentProcessedMessage = MessageBuilder.withPayload(paymentEvent)
//                .setHeader(KafkaHeaders.KEY, paymentPolicyId.toString())
//                .setHeader("eventType", "PAYMENT_PROCESSED")
//                .setHeader("eventSourceId", "test-instance")
//                .build();
//
//        kafkaTemplate.send("payment-confirmed-events-topic-test", paymentProcessedMessage).get(60, TimeUnit.SECONDS);
//    }
//}