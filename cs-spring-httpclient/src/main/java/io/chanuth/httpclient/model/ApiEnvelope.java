package io.chanuth.httpclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The Task API wraps every successful answer as {"data": ...}. This record peels that wrapper off.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiEnvelope<T>(T data) {
}
