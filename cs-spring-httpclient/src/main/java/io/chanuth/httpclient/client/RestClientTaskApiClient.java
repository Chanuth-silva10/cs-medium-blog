package io.chanuth.httpclient.client;

import io.chanuth.httpclient.config.TaskApiProperties;
import io.chanuth.httpclient.model.ApiEnvelope;
import io.chanuth.httpclient.model.CategoryDto;
import io.chanuth.httpclient.model.CreateCategoryRequest;
import io.chanuth.httpclient.model.CreateTaskRequest;
import io.chanuth.httpclient.model.LoginRequest;
import io.chanuth.httpclient.model.LoginResponse;
import io.chanuth.httpclient.model.TaskDto;
import io.chanuth.httpclient.model.UpdateTaskRequest;
import java.time.Clock;
import java.util.List;
import java.util.function.Function;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

/**
 * The same client written with RestClient, the fluent replacement for RestTemplate.
 */
public class RestClientTaskApiClient implements TaskApiClient {

    private static final ParameterizedTypeReference<ApiEnvelope<TaskDto>> TASK =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<ApiEnvelope<List<TaskDto>>> TASK_LIST =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<ApiEnvelope<CategoryDto>> CATEGORY =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<ApiEnvelope<List<CategoryDto>>> CATEGORY_LIST =
            new ParameterizedTypeReference<>() { };

    private final RestClient restClient;
    private final TokenCache tokens;

    /** The RestClient must already point at the base URL and use the error handling from {@link #withErrorHandling}. */
    public RestClientTaskApiClient(RestClient restClient, TaskApiProperties properties) {
        this.restClient = restClient;
        this.tokens = new TokenCache(() -> login(properties), Clock.systemUTC());
    }

    /** Teaches a RestClient.Builder to throw our exception for any error status. */
    public static RestClient.Builder withErrorHandling(RestClient.Builder builder) {
        return builder.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
            throw TaskApiErrors.fromResponse(response.getStatusCode().value(), response.getBody());
        });
    }

    private LoginResponse login(TaskApiProperties properties) {
        try {
            return restClient.post()
                    .uri("/auth/login")
                    .body(new LoginRequest(properties.email(), properties.password()))
                    .retrieve()
                    .body(LoginResponse.class);
        } catch (TaskApiException e) {
            throw new TaskApiException(401, "The Task API rejected the service account: " + e.getMessage());
        }
    }

    @Override
    public List<TaskDto> myTasks() {
        return authorized(token -> restClient.get().uri("/tasks/me")
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(TASK_LIST).data());
    }

    @Override
    public TaskDto getTask(String id) {
        return authorized(token -> restClient.get().uri("/tasks/{id}", id)
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(TASK).data());
    }

    @Override
    public TaskDto createTask(CreateTaskRequest request) {
        return authorized(token -> restClient.post().uri("/tasks")
                .headers(h -> h.setBearerAuth(token))
                .body(request)
                .retrieve().body(TASK).data());
    }

    @Override
    public TaskDto updateTask(String id, UpdateTaskRequest request) {
        return authorized(token -> restClient.patch().uri("/tasks/{id}", id)
                .headers(h -> h.setBearerAuth(token))
                .body(request)
                .retrieve().body(TASK).data());
    }

    @Override
    public void deleteTask(String id) {
        authorized(token -> restClient.delete().uri("/tasks/{id}", id)
                .headers(h -> h.setBearerAuth(token))
                .retrieve().toBodilessEntity());
    }

    @Override
    public List<CategoryDto> categories() {
        return authorized(token -> restClient.get().uri("/categories")
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(CATEGORY_LIST).data());
    }

    @Override
    public CategoryDto createCategory(CreateCategoryRequest request) {
        return authorized(token -> restClient.post().uri("/categories")
                .headers(h -> h.setBearerAuth(token))
                .body(request)
                .retrieve().body(CATEGORY).data());
    }

    /** Same one-retry-on-401 rule as the RestTemplate version. */
    private <T> T authorized(Function<String, T> call) {
        String token = tokens.get();
        try {
            return call.apply(token);
        } catch (TaskApiException e) {
            if (e.status() != 401) {
                throw e;
            }
            tokens.invalidate();
            return call.apply(tokens.get());
        }
    }
}
