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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * The Task API client written with RestTemplate.
 */
public class RestTemplateTaskApiClient implements TaskApiClient {

    // The API wraps answers as {"data": ...}. Generics are erased at runtime, so to read
    // ApiEnvelope<TaskDto> we pass the type along in a ParameterizedTypeReference.
    private static final ParameterizedTypeReference<ApiEnvelope<TaskDto>> TASK =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<ApiEnvelope<List<TaskDto>>> TASK_LIST =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<ApiEnvelope<CategoryDto>> CATEGORY =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<ApiEnvelope<List<CategoryDto>>> CATEGORY_LIST =
            new ParameterizedTypeReference<>() { };

    private final RestTemplate restTemplate;
    private final TokenCache tokens;

    public RestTemplateTaskApiClient(RestTemplate restTemplate, TaskApiProperties properties) {
        this.restTemplate = restTemplate;
        this.tokens = new TokenCache(() -> login(properties), Clock.systemUTC());
    }

    private LoginResponse login(TaskApiProperties properties) {
        try {
            return restTemplate.postForObject("/auth/login",
                    new LoginRequest(properties.email(), properties.password()), LoginResponse.class);
        } catch (TaskApiException e) {
            // A bad service account is our configuration problem, not something the caller did.
            throw new TaskApiException(401, "The Task API rejected the service account: " + e.getMessage());
        }
    }

    @Override
    public List<TaskDto> myTasks() {
        return authorized(headers -> restTemplate.exchange(
                "/tasks/me", HttpMethod.GET, new HttpEntity<>(headers), TASK_LIST).getBody().data());
    }

    @Override
    public TaskDto getTask(String id) {
        // The {id} placeholder is encoded for us. Never glue a caller's text into a URL by hand.
        return authorized(headers -> restTemplate.exchange(
                "/tasks/{id}", HttpMethod.GET, new HttpEntity<>(headers), TASK, id).getBody().data());
    }

    @Override
    public TaskDto createTask(CreateTaskRequest request) {
        return authorized(headers -> restTemplate.exchange(
                "/tasks", HttpMethod.POST, new HttpEntity<>(request, headers), TASK).getBody().data());
    }

    @Override
    public TaskDto updateTask(String id, UpdateTaskRequest request) {
        return authorized(headers -> restTemplate.exchange(
                "/tasks/{id}", HttpMethod.PATCH, new HttpEntity<>(request, headers), TASK, id).getBody().data());
    }

    @Override
    public void deleteTask(String id) {
        authorized(headers -> restTemplate.exchange(
                "/tasks/{id}", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class, id));
    }

    @Override
    public List<CategoryDto> categories() {
        return authorized(headers -> restTemplate.exchange(
                "/categories", HttpMethod.GET, new HttpEntity<>(headers), CATEGORY_LIST).getBody().data());
    }

    @Override
    public CategoryDto createCategory(CreateCategoryRequest request) {
        return authorized(headers -> restTemplate.exchange(
                "/categories", HttpMethod.POST, new HttpEntity<>(request, headers), CATEGORY).getBody().data());
    }

    /**
     * Runs a call with a bearer token. If the API says 401 the token probably expired early, so we
     * forget it, log in again and try exactly once more. A 401 means the request was rejected before
     * it did anything, so repeating it, even a POST, cannot create duplicates.
     */
    private <T> T authorized(Function<HttpHeaders, T> call) {
        String token = tokens.get();
        try {
            return call.apply(headersWith(token));
        } catch (TaskApiException e) {
            if (e.status() != 401) {
                throw e;
            }
            tokens.invalidate();
            return call.apply(headersWith(tokens.get()));
        }
    }

    private static HttpHeaders headersWith(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }
}
