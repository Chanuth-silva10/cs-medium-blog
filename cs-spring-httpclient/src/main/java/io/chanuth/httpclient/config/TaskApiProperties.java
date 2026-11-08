package io.chanuth.httpclient.config;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Settings for talking to the DevCraft Task API. Because the class is validated, a missing
 * base URL or password stops the application at startup with a clear message,
 * instead of failing on the first request.
 */
@Validated
@ConfigurationProperties(prefix = "devcraft.task-api")
public record TaskApiProperties(
        @NotBlank String baseUrl,
        @NotBlank String email,
        @NotBlank String password,
        @DefaultValue("rest-template") ClientType client,
        @DefaultValue("2s") Duration connectTimeout,
        @DefaultValue("5s") Duration readTimeout) {

    public enum ClientType {
        REST_TEMPLATE,
        REST_CLIENT
    }
}
