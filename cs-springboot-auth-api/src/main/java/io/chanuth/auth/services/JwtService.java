package io.chanuth.auth.services;

import io.chanuth.auth.configs.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Creates and reads the JSON Web Tokens. A token carries the user's email as its subject and
 * is signed with HMAC-SHA256, so nobody can change it without the secret key.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final Duration expiration;

    public JwtService(JwtProperties properties) {
        byte[] keyBytes = Decoders.BASE64.decode(properties.secretKey());
        if (keyBytes.length < 32) {
            // HS256 needs at least 256 bits. Fail at startup, not at the first login.
            throw new IllegalStateException(
                    "security.jwt.secret-key must be a Base64 value of at least 32 bytes (256 bits)");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = properties.expiration();
    }

    public String generateToken(UserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Returns the email inside a token. Throws a JwtException if the token was tampered with,
     * signed with another key, malformed or expired, so a token that gets past here can be trusted.
     */
    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public long expirationInSeconds() {
        return expiration.toSeconds();
    }

    private Claims parse(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
