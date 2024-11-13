package io.aslan.notificationservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.aslan.notificationservice.client.PexelsSearchResponse;
import io.aslan.notificationservice.containers.PexelsContainer;
import io.aslan.notificationservice.domain.message.AllowanceUpdateMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.VerifyEmailAddressRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.aslan.notificationservice.client.PexelsSearchResponse.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SES;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class NotificationServiceIntegrationTest {

    private static final String FROM_EMAIL_ADDRESS = "notifications@email.com";

    private static SqsClient sqsClient;
    private static SesClient sesClient;
    private static String notificationServiceQueueUrl;

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withReuse(true);

    @Container
    static LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.5.0"))
            .withServices(SQS, SES)
            .withReuse(true);

    @Container
    static PexelsContainer pexelsContainer = new PexelsContainer()
            .withReuse(true);
    private static WireMock pexelsClient;

    @BeforeEach
    public void resetWireMock() {
        pexelsClient.resetMappings();
        pexelsClient.resetRequests();
        pexelsClient.resetScenarios();
        pexelsClient.resetToDefaultMappings();
    }

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        sesClient = SesClient.builder()
                .region(Region.of(localStackContainer.getRegion()))
                .endpointOverride(localStackContainer.getEndpointOverride(SQS))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localStackContainer.getAccessKey(), localStackContainer.getSecretKey())
                ))
                .build();

        sqsClient = SqsClient.builder()
                .region(Region.of(localStackContainer.getRegion()))
                .endpointOverride(localStackContainer.getEndpointOverride(SQS))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localStackContainer.getAccessKey(), localStackContainer.getSecretKey())
                ))
                .build();

        sesClient.verifyEmailAddress(VerifyEmailAddressRequest.builder()
                        .emailAddress(FROM_EMAIL_ADDRESS)
                .build());

        CreateQueueResponse createQueueResponse = sqsClient.createQueue(CreateQueueRequest.builder()
                .queueName("notification-service-queue")
                .build());
        notificationServiceQueueUrl = createQueueResponse.queueUrl();

        pexelsClient = new WireMock(pexelsContainer.getHost(), pexelsContainer.getFirstMappedPort());

        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("pexels.baseUrl", () -> "http://" + pexelsContainer.getBaseUrl());
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.cloud.aws.region.static", () -> localStackContainer.getRegion());
        registry.add("spring.cloud.aws.credentials.access-key", () -> localStackContainer.getAccessKey());
        registry.add("spring.cloud.aws.credentials.secret-key", () -> localStackContainer.getSecretKey());
        registry.add("spring.cloud.aws.sqs.endpoint", () -> localStackContainer.getEndpointOverride(SQS).toString());
        registry.add("spring.cloud.aws.ses.endpoint", () -> localStackContainer.getEndpointOverride(SES).toString());
        registry.add("source.email", () -> FROM_EMAIL_ADDRESS);
        registry.add("notification.service.queue.url", () -> notificationServiceQueueUrl);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldReceiveAllowanceUpdateMessageAndSendCalmEmailUser() {

        pexelsClient.register(get(urlPathEqualTo("/search"))
                .withQueryParam("query", equalTo("calm"))
                .withQueryParam("per_page", equalTo("5"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(pexelsResponse("calm"))));

        String email = "david@kilan.com";

        BigDecimal currentAllowance = BigDecimal.TWO;
        BigDecimal newAllowance = BigDecimal.ONE;
        var sqsMessageId = sendMessageToQueue(employeeMessage(email, currentAllowance, newAllowance));

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    String countSql = "SELECT COUNT(*) FROM email_audit WHERE id = ?";
                    Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, sqsMessageId);
                    return count > 0;
                });

        String sql = "SELECT * FROM email_audit WHERE id = ?";
        Map<String, Object> dbResult = jdbcTemplate.queryForMap(sql, sqsMessageId);

        assertThat(dbResult)
                .hasSize(6)
                .containsEntry("id", sqsMessageId)
                .hasEntrySatisfying("email_message_id", (actual) -> assertThat(actual).isNotNull())
                .containsEntry("to_address", email)
                .containsEntry("subject", "Allowance Change.")
                .hasEntrySatisfying("body", (actual) -> {
                    assertThat((String) actual).contains("Your allowance has been changed");
                    assertThat((String) actual).contains("https://photo-calm");
                })
                .hasEntrySatisfying("created_at", actual -> {
                    Timestamp actualTimestamp = (Timestamp) actual;
                    LocalDateTime actualDateTime = actualTimestamp.toLocalDateTime();
                    assertThat(actualDateTime)
                            .isBefore(LocalDateTime.now())
                            .isAfter(LocalDateTime.now().minusMinutes(1));
                });
    }

    @Test
    public void shouldReceiveAllowanceUpdateMessageAndSendHappyEmailUser() {

        pexelsClient.register(get(urlPathEqualTo("/search"))
                .withQueryParam("query", equalTo("happy"))
                .withQueryParam("per_page", equalTo("5"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(pexelsResponse("happy"))));

        String email = "david@kilan.com";

        BigDecimal currentAllowance = BigDecimal.ONE;
        BigDecimal newAllowance = BigDecimal.TWO;
        var sqsMessageId = sendMessageToQueue(employeeMessage(email, currentAllowance, newAllowance));

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    String countSql = "SELECT COUNT(*) FROM email_audit WHERE id = ?";
                    Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, sqsMessageId);
                    return count > 0;
                });

        String sql = "SELECT * FROM email_audit WHERE id = ?";
        Map<String, Object> dbResult = jdbcTemplate.queryForMap(sql, sqsMessageId);

        assertThat(dbResult)
                .hasSize(6)
                .containsEntry("id", sqsMessageId)
                .hasEntrySatisfying("email_message_id", (actual) -> assertThat(actual).isNotNull())
                .containsEntry("to_address", email)
                .containsEntry("subject", "Allowance Change.")
                .hasEntrySatisfying("body", (actual) -> {
                    assertThat((String) actual).contains("Great news! Your allowance is increasing.");
                    assertThat((String) actual).contains("https://photo-happy");
                })
                .hasEntrySatisfying("created_at", actual -> {
                    Timestamp actualTimestamp = (Timestamp) actual;
                    LocalDateTime actualDateTime = actualTimestamp.toLocalDateTime();
                    assertThat(actualDateTime)
                            .isBefore(LocalDateTime.now())
                            .isAfter(LocalDateTime.now().minusMinutes(1));
                });
    }

    private static String pexelsResponse(String content) {
        List<Photos> photos = IntStream.range(1, 6).mapToObj(id -> new Photos(new Src("https://photo-" + content + "-" + id))).toList();
        PexelsSearchResponse pexelsSearchResponse = new PexelsSearchResponse(photos);
        try {
            return objectMapper.writeValueAsString(pexelsSearchResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static UUID sendMessageToQueue(Object payload) {
        try {
            SendMessageResponse sendMessageResponse = sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(notificationServiceQueueUrl)
                    .messageBody(objectMapper.writeValueAsString(payload))
                    .build());
            return UUID.fromString(sendMessageResponse.messageId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static AllowanceUpdateMessage employeeMessage(String email, BigDecimal currentMonthlyAllowance, BigDecimal newMonthlyAllowance) {
        return new AllowanceUpdateMessage(1L,
                "David",
                "Kilan",
                currentMonthlyAllowance,
                newMonthlyAllowance,
                email);
    }
}