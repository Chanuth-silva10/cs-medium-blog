package io.chanuth.httpclient;

import static org.assertj.core.api.Assertions.assertThat;

import io.chanuth.httpclient.client.RestTemplateTaskApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "devcraft.task-api.client=rest-template")
class RestTemplateClientTest extends TaskApiContractTest {

    @Test
    void usesTheRestTemplateImplementation() {
        assertThat(taskApiClient).isInstanceOf(RestTemplateTaskApiClient.class);
    }
}
