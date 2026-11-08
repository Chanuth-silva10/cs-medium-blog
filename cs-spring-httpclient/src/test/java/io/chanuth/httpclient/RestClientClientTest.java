package io.chanuth.httpclient;

import static org.assertj.core.api.Assertions.assertThat;

import io.chanuth.httpclient.client.RestClientTaskApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "devcraft.task-api.client=rest-client")
class RestClientClientTest extends TaskApiContractTest {

    @Test
    void usesTheRestClientImplementation() {
        assertThat(taskApiClient).isInstanceOf(RestClientTaskApiClient.class);
    }
}
