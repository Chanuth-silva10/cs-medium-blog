package io.chanuth.httpclient.config;

import io.chanuth.httpclient.client.RestClientTaskApiClient;
import io.chanuth.httpclient.client.RestTemplateTaskApiClient;
import io.chanuth.httpclient.client.TaskApiClient;
import io.chanuth.httpclient.client.TaskApiResponseErrorHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/**
 * Builds exactly one {@link TaskApiClient}. Set devcraft.task-api.client to rest-template (the
 * default) or rest-client to choose which implementation the application uses.
 */
@Configuration
public class HttpClientConfig {

    @Bean
    @ConditionalOnProperty(name = "devcraft.task-api.client", havingValue = "rest-template", matchIfMissing = true)
    TaskApiClient restTemplateTaskApiClient(RestTemplateBuilder builder, TaskApiProperties properties) {
        // A plain "new RestTemplate()" has NO timeouts: a stuck server would hold a thread forever.
        RestTemplate restTemplate = builder
                .rootUri(properties.baseUrl())
                .connectTimeout(properties.connectTimeout())
                .readTimeout(properties.readTimeout())
                .errorHandler(new TaskApiResponseErrorHandler())
                .build();
        return new RestTemplateTaskApiClient(restTemplate, properties);
    }

    @Bean
    @ConditionalOnProperty(name = "devcraft.task-api.client", havingValue = "rest-client")
    TaskApiClient restClientTaskApiClient(RestClient.Builder builder, TaskApiProperties properties) {
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect().build(
                HttpClientSettings.defaults()
                        .withConnectTimeout(properties.connectTimeout())
                        .withReadTimeout(properties.readTimeout()));
        RestClient restClient = RestClientTaskApiClient.withErrorHandling(builder)
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
        return new RestClientTaskApiClient(restClient, properties);
    }
}
