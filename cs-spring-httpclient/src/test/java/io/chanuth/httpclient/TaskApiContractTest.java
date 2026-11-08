package io.chanuth.httpclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import io.chanuth.httpclient.client.TaskApiClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * One set of tests, run once per HTTP client. Both RestTemplate and RestClient must behave identically.
 */
@SpringBootTest
@AutoConfigureMockMvc
abstract class TaskApiContractTest {

    private static final TaskApiStub STUB = new TaskApiStub();

    @DynamicPropertySource
    static void taskApiProperties(DynamicPropertyRegistry registry) {
        registry.add("devcraft.task-api.base-url", STUB::baseUrl);
        registry.add("devcraft.task-api.email", () -> TaskApiStub.EMAIL);
        registry.add("devcraft.task-api.password", () -> TaskApiStub.PASSWORD);
        registry.add("devcraft.task-api.read-timeout", () -> "400ms");
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    TaskApiClient taskApiClient;

    @BeforeEach
    void resetStub() {
        STUB.reset();
    }

    @Test
    void listsMyTasksAndReadsTheCategoryObject() throws Exception {
        mvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("t1"))
                .andExpect(jsonPath("$[0].status").value("TODO"))
                .andExpect(jsonPath("$[0].category.id").value("c1"))
                .andExpect(jsonPath("$[0].category.name").value("Work"));
    }

    @Test
    void createsATaskAndReadsTheCategoryIdOnly() throws Exception {
        mvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Write docs\",\"category\":\"c1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("t2"))
                .andExpect(jsonPath("$.category.id").value("c1"));

        // Fields the caller left out must not be sent at all, not even as null.
        assertThat(STUB.lastBody()).contains("\"title\":\"Write docs\"").doesNotContain("description").doesNotContain("null");
    }

    @Test
    void rejectsAnInvalidTaskWithoutCallingTheTaskApi() throws Exception {
        mvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.category").exists());

        assertThat(STUB.requestCount()).isZero();
    }

    @Test
    void sendsOnlyTheFieldsThatChange() throws Exception {
        mvc.perform(patch("/tasks/t1").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        assertThat(STUB.lastBody()).isEqualTo("{\"status\":\"DONE\"}");
    }

    @Test
    void passesOnANotFoundWithTheUpstreamMessage() throws Exception {
        mvc.perform(get("/tasks/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task with id \"missing\" not found."));
    }

    @Test
    void encodesPathVariablesSoCallersCannotChangeTheUrl() throws Exception {
        // An id smuggling in "../" and a query string. Glued into the URL by hand, this would call
        // "/categories?x=1". With a {id} placeholder it stays inside one path segment.
        taskApiClient.getTask("../categories?x=1");

        assertThat(STUB.lastRawPath()).startsWith("/tasks/").doesNotContain("?").doesNotContain("/categories");
    }
    @Test
    void passesOnForbiddenAndAcceptsAllowedDeletes() throws Exception {
        mvc.perform(delete("/tasks/other"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden: your role cannot do this"));
        mvc.perform(delete("/tasks/ok")).andExpect(status().isNoContent());
    }

    @Test
    void logsInOnceAndReusesTheToken() throws Exception {
        mvc.perform(get("/tasks")).andExpect(status().isOk());
        int loginsAfterFirstCall = STUB.loginCount();

        mvc.perform(get("/tasks")).andExpect(status().isOk());
        mvc.perform(get("/categories")).andExpect(status().isOk());

        assertThat(STUB.loginCount()).isEqualTo(loginsAfterFirstCall);
    }
    @Test
    void logsInAgainWhenTheTokenIsRejected() throws Exception {
        mvc.perform(get("/tasks")).andExpect(status().isOk());
        STUB.revokeTokens();
        int loginsBefore = STUB.loginCount();

        mvc.perform(get("/tasks")).andExpect(status().isOk());

        assertThat(STUB.loginCount()).isEqualTo(loginsBefore + 1);
    }

    @Test
    void answers502WhenOurServiceAccountIsRefused() throws Exception {
        mvc.perform(get("/tasks")).andExpect(status().isOk());
        STUB.revokeTokens();
        STUB.rejectLogins(true);

        mvc.perform(get("/tasks"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("The Task API could not complete the request"));
    }

    @Test
    void answers502WhenTheTaskApiFails() throws Exception {
        mvc.perform(get("/tasks/boom")).andExpect(status().isBadGateway());
        mvc.perform(get("/tasks/html")).andExpect(status().isBadGateway());
    }

    @Test
    void answers504WhenTheTaskApiIsTooSlow() throws Exception {
        mvc.perform(get("/tasks/slow"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.message").value("The Task API took too long to answer"));
    }
}
