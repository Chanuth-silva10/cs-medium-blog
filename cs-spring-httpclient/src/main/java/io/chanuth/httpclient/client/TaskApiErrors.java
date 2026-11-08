package io.chanuth.httpclient.client;

import java.io.InputStream;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Turns an error response from the Task API into a {@link TaskApiException}. The API explains its
 * errors as {"message": "..."}, so we surface that text instead of a bare status code.
 */
final class TaskApiErrors {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private TaskApiErrors() {
    }

    static TaskApiException fromResponse(int status, InputStream body) {
        String message = "The Task API answered with status " + status;
        try {
            JsonNode node = MAPPER.readTree(body);
            JsonNode text = node.path("message");
            if (text.isString() && !text.asString().isBlank()) {
                message = text.asString();
            }
        } catch (RuntimeException ignored) {
            // The body was empty or not JSON, so the generic message above is the best we have.
        }
        return new TaskApiException(status, message);
    }
}
