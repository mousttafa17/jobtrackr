package com.moustafa.jobtrackr.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerLoginAndMeWorkAgainstPostgres() {
        String email = "it-" + UUID.randomUUID() + "@jobtrackr.com";

        Map<String, Object> registerResponse = register("Integration Tester", email, "password123");
        assertThat(registerResponse.get("token")).isInstanceOf(String.class);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                Map.of("email", email, "password", "password123"),
                Map.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = (String) loginResponse.getBody().get("token");
        assertThat(token).isNotBlank();

        ResponseEntity<Map> meResponse = restTemplate.exchange(
                "/api/auth/me",
                HttpMethod.GET,
                authenticatedRequest(token),
                Map.class
        );

        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(meResponse.getBody().get("email")).isEqualTo(email);
    }

    @Test
    void protectedApplicationRoutesRequireJwtAndEnforceOwnership() {
        ResponseEntity<Map> unauthenticatedResponse = restTemplate.getForEntity("/api/applications", Map.class);

        assertThat(unauthenticatedResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        String ownerToken = tokenForNewUser("Owner User");
        Long applicationId = createApplication(ownerToken, "OpenAI", "Backend Engineer");

        String otherUserToken = tokenForNewUser("Other User");
        ResponseEntity<Map> otherUserResponse = restTemplate.exchange(
                "/api/applications/" + applicationId,
                HttpMethod.GET,
                authenticatedRequest(otherUserToken),
                Map.class
        );

        assertThat(otherUserResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void statusHistoryTracksApplicationStatusChanges() {
        String token = tokenForNewUser("Status History Tester");
        Long applicationId = createApplication(token, "Stripe", "Java Engineer");

        ResponseEntity<Map> statusResponse = restTemplate.exchange(
                "/api/applications/" + applicationId + "/status",
                HttpMethod.PATCH,
                authenticatedRequest(token, Map.of("status", "OFFER")),
                Map.class
        );

        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResponse.getBody().get("status")).isEqualTo("OFFER");

        List<?> history = getList("/api/applications/" + applicationId + "/status-history", token);

        assertThat(history).hasSize(2);
        assertThat(castMap(history.get(0)).get("oldStatus")).isEqualTo("INTERVIEW");
        assertThat(castMap(history.get(0)).get("newStatus")).isEqualTo("OFFER");
        assertThat(castMap(history.get(1)).get("oldStatus")).isNull();
        assertThat(castMap(history.get(1)).get("newStatus")).isEqualTo("INTERVIEW");
    }

    @Test
    void authenticatedUserCanManageApplicationChildResources() {
        String token = tokenForNewUser("Child Resource Tester");
        Long applicationId = createApplication(token, "Google", "Backend Engineer");

        Long interviewId = postForId(
                "/api/applications/" + applicationId + "/interviews",
                token,
                Map.of(
                        "type", "TECHNICAL",
                        "scheduledAt", "2026-06-10T15:00:00Z",
                        "location", "Remote"
                )
        );
        Long taskId = postForId(
                "/api/applications/" + applicationId + "/tasks",
                token,
                Map.of(
                        "title", "Send thank-you email",
                        "description", "Mention the Spring Boot discussion",
                        "dueAt", "2026-06-11T12:00:00Z"
                )
        );
        Long noteId = postForId(
                "/api/applications/" + applicationId + "/notes",
                token,
                Map.of("content", "Recruiter said feedback should arrive next week.")
        );

        assertThat(idsFrom(getList("/api/applications/" + applicationId + "/interviews", token)))
                .contains(interviewId);
        assertThat(idsFrom(getList("/api/applications/" + applicationId + "/tasks", token)))
                .contains(taskId);
        assertThat(idsFrom(getList("/api/applications/" + applicationId + "/notes", token)))
                .contains(noteId);

        ResponseEntity<Map> completedTaskResponse = restTemplate.exchange(
                "/api/tasks/" + taskId + "/complete",
                HttpMethod.PATCH,
                authenticatedRequest(token),
                Map.class
        );

        assertThat(completedTaskResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(completedTaskResponse.getBody().get("completed")).isEqualTo(true);
    }

    @Test
    void openApiDocsArePublicAndAdvertiseBearerAuth() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/v3/api-docs", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) ((Map<?, ?>) response.getBody().get("info")).get("title")).isEqualTo("JobTrackr API");
        Map<String, Object> components = castMap(response.getBody().get("components"));
        Map<String, Object> securitySchemes = castMap(components.get("securitySchemes"));
        assertThat(securitySchemes).containsKey("bearerAuth");
    }

    private String tokenForNewUser(String fullName) {
        String email = "it-" + UUID.randomUUID() + "@jobtrackr.com";
        return (String) register(fullName, email, "password123").get("token");
    }

    private Map<String, Object> register(String fullName, String email, String password) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/register",
                Map.of("fullName", fullName, "email", email, "password", password),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private Long createApplication(String token, String companyName, String jobTitle) {
        return postForId(
                "/api/applications",
                token,
                Map.of(
                        "companyName", companyName,
                        "jobTitle", jobTitle,
                        "status", "INTERVIEW"
                )
        );
    }

    private Long postForId(String path, String token, Map<String, Object> request) {
        ResponseEntity<Map> response = restTemplate.exchange(
                path,
                HttpMethod.POST,
                authenticatedRequest(token, request),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return ((Number) response.getBody().get("id")).longValue();
    }

    private List<?> getList(String path, String token) {
        ResponseEntity<List> response = restTemplate.exchange(
                path,
                HttpMethod.GET,
                authenticatedRequest(token),
                List.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private List<Long> idsFrom(List<?> items) {
        return items.stream()
                .map(item -> ((Number) castMap(item).get("id")).longValue())
                .toList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    private HttpEntity<Void> authenticatedRequest(String token) {
        return new HttpEntity<>(headersWithToken(token));
    }

    private HttpEntity<Map<String, Object>> authenticatedRequest(String token, Map<String, Object> body) {
        return new HttpEntity<>(body, headersWithToken(token));
    }

    private HttpHeaders headersWithToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
