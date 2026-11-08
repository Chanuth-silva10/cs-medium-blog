package io.chanuth.httpclient.client;

import io.chanuth.httpclient.model.LoginResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * Keeps the Task API token so we log in once, not before every call. It renews the token a little
 * before it expires, and forgets it when the API says it was rejected.
 */
final class TokenCache {

    private static final Duration SAFETY_MARGIN = Duration.ofSeconds(30);

    private final Supplier<LoginResponse> loginCall;
    private final Clock clock;

    private String token;
    private Instant renewAt = Instant.MIN;

    TokenCache(Supplier<LoginResponse> loginCall, Clock clock) {
        this.loginCall = loginCall;
        this.clock = clock;
    }

    /** Several requests can arrive at once, so only one of them is allowed to log in. */
    synchronized String get() {
        if (token == null || !clock.instant().isBefore(renewAt)) {
            LoginResponse login = loginCall.get();
            token = login.token();
            renewAt = clock.instant().plusSeconds(login.expiresIn()).minus(SAFETY_MARGIN);
        }
        return token;
    }

    synchronized void invalidate() {
        token = null;
    }
}
