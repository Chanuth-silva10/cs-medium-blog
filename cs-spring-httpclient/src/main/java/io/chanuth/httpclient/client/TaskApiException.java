package io.chanuth.httpclient.client;

/**
 * Thrown when the Task API answers with an error status. One exception type for both HTTP clients
 * means the rest of the application does not depend on which client is in use.
 */
public class TaskApiException extends RuntimeException {

    private final int status;

    public TaskApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
