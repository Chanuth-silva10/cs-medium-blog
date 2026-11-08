package io.chanuth.auth.configs;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * The signing key comes from the environment, never from the code or the repository.
 * Without it the application refuses to start.
 */
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(@NotBlank String secretKey, @DefaultValue("1h") Duration expiration) {
}
