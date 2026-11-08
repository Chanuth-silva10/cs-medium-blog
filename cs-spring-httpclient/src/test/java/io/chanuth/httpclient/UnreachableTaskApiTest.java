package io.chanuth.httpclient;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.net.ServerSocket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Nothing listens on the Task API address, as if the service were down. The answer must be a clean 503.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UnreachableTaskApiTest {

    @DynamicPropertySource
    static void closedPort(DynamicPropertyRegistry registry) throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        registry.add("devcraft.task-api.base-url", () -> "http://127.0.0.1:" + port);
        registry.add("devcraft.task-api.email", () -> "svc@devcraft.io");
        registry.add("devcraft.task-api.password", () -> "secret");
    }

    @Autowired
    MockMvc mvc;

    @Test
    void answers503WhenTheTaskApiIsDown() throws Exception {
        mvc.perform(get("/tasks"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("The Task API is unreachable"));
    }
}
