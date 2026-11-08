package io.chanuth.httpclient.client;

import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * By default RestTemplate throws different exceptions for 4xx and 5xx answers and throws away what
 * the server said. This handler replaces them with our own {@link TaskApiException}.
 */
public class TaskApiResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        throw TaskApiErrors.fromResponse(response.getStatusCode().value(), response.getBody());
    }
}
