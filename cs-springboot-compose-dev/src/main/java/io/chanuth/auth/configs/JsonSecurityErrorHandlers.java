package io.chanuth.auth.configs;

import io.chanuth.auth.dtos.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Spring Security answers 401 and 403 itself, before any controller runs, and by default with an
 * empty body. These two handlers make those answers explain themselves in our usual JSON shape.
 */
@Component
public class JsonSecurityErrorHandlers {

    private final JsonMapper jsonMapper;

    public JsonSecurityErrorHandlers(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /** 401: we do not know who you are. */
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> {
            Object reason = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR);
            write(response, HttpStatus.UNAUTHORIZED, reason != null ? reason.toString() : "Authentication is required");
        };
    }

    /** 403: we know who you are, and you may not do this. */
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) ->
                write(response, HttpStatus.FORBIDDEN, "You do not have permission to access this resource");
    }

    private void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), ErrorResponse.of(message));
    }
}
