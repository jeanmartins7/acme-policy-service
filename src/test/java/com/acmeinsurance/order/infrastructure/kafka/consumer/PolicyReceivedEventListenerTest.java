//package com.acmeinsurance.order.infrastructure.kafka.consumer;
//
//import com.acmeinsurance.order.Application;
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
//import org.junit.Ignore;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.SpyBean;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.KafkaContainer;
//import org.testcontainers.containers.Network;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.containers.wait.strategy.Wait; // Importação adicionada para espera
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.utility.DockerImageName;
//import reactor.core.publisher.Mono;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
//import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
//import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
//import java.math.BigDecimal;
//import java.net.URI;
//import java.time.Instant;
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
//@Ignore
//class PolicyReceivedEventListenerIntegrationTest {
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
//    // Adição do contêiner DynamoDB Local
//    @Container
//    public static GenericContainer<?> dynamoDb = new GenericContainer<>(DockerImageName.parse("amazon/dynamodb-local:latest"))
//            .withNetwork(network)
//            .withNetworkAliases("dynamodb-local")
//            .withExposedPorts(8000) // Porta padrão do DynamoDB Local
//            .waitingFor(Wait.forLogMessage(".*CorsParams.AllowedOrigins=.*\\n", 1)); // Espera por uma mensagem no log
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
//        registry.add("fraud.api.url", () -> "http://localhost:8080");
//
//        // Configurações dinâmicas para o DynamoDB
//        registry.add("aws.dynamodb.endpoint-override", () -> "http://" + dynamoDb.getHost() + ":" + dynamoDb.getMappedPort(8000));
//        registry.add("aws.region", () -> "us-east-1"); // Região arbitrária para o teste
//        registry.add("aws.accessKeyId", () -> "test"); // Credenciais de teste
//        registry.add("aws.secretAccessKey", () -> "test"); // Credenciais de teste
//    }
//
//    @Autowired
//    private KafkaTemplate<String, Object> kafkaTemplate;
//
//    @SpyBean
//    private PolicyRequestRepository policyRequestRepository;
//
//    @SpyBean
//    private FraudApiService fraudApiService;
//
//    private static AdminClient adminClient;
//
//    // Cliente DynamoDB Enhanced para criar a tabela de teste
//    private static DynamoDbEnhancedAsyncClient enhancedDynamoDbClient;
//
//
//    @BeforeAll
//    static void setupContainers() {
//        // Setup Kafka (mantido do seu código anterior)
//        adminClient = AdminClient.create(Collections.singletonMap("bootstrap.servers", kafka.getBootstrapServers()));
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
//
//        // Setup DynamoDB Local: criar o cliente e a tabela
//        DynamoDbAsyncClient asyncClient = DynamoDbAsyncClient.builder()
//                .endpointOverride(URI.create("http://" + dynamoDb.getHost() + ":" + dynamoDb.getMappedPort(8000)))
//                .region(Region.of("us-east-1")) // Use a mesma região configurada
//                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
//                .build();
//
//        enhancedDynamoDbClient = DynamoDbEnhancedAsyncClient.builder()
//                .dynamoDbClient(asyncClient)
//                .build();
//
//        // Criar a tabela 'policy-requests' (ou o nome da sua tabela)
//        // Você precisará do TableSchema para a sua classe PolicyRequest.class
//        try {
//            // Supondo que você tem um PolicyRequest.class mapeado com @DynamoDbBean
//            enhancedDynamoDbClient.table("policy-requests", TableSchema.fromBean(PolicyRequest.class))
//                    .createTable().get(30, TimeUnit.SECONDS); // Espera a criação da tabela
//            System.out.println("DynamoDB table 'policy-requests' created successfully.");
//        } catch (Exception e) {
//            System.err.println("Failed to create DynamoDB table: " + e.getMessage());
//            e.printStackTrace();
//            throw new RuntimeException("Failed to create DynamoDB table for test", e);
//        }
//    }
//
//    @BeforeEach
//    void setup() {
//        reset(policyRequestRepository, fraudApiService);
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
//        // O mock do save() do policyRequestRepository pode ser removido ou alterado para
//        // verificar se o método save original do DynamoDB é chamado, se você quiser testar a persistência real.
//        // No entanto, para o erro NoSuchElementException, a causa é a falta do DynamoDB Local.
//        // Mantenho o mock para fins de compatibilidade com o seu setup original.
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
//                .setCreatedAt(initialPolicyRequest.getCreatedAt().toString())
//                .build();
//
//        kafkaTemplate.send("policy-status-notifications-topic-test", avroEvent.getPolicyId(), avroEvent).get(10, TimeUnit.SECONDS);
//
//        verify(fraudApiService, timeout(10000)).analyzePolicyRequest(eq(initialPolicyRequest.getId()), eq(initialPolicyRequest.getCustomerId()));
//        verify(policyRequestRepository, timeout(10000)).save(any(PolicyRequest.class));
//        verify(policyRequestRepository, timeout(10000)).save(argThat(policy ->
//                policy.getId().equals(initialPolicyRequest.getId()) &&
//                        policy.getStatus().equals(PolicyStatusEnum.VALIDATED)
//        ));
//    }
//}