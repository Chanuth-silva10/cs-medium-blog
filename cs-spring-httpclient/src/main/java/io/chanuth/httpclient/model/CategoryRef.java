package io.chanuth.httpclient.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Map;

/**
 * The Task API is inconsistent about a task's category: right after a task is created it sends just
 * the category id ("665f..."), but when tasks are listed it sends the whole category object.
 * This type accepts both, so the rest of our code never has to care.
 */
public record CategoryRef(String id, String name) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    static CategoryRef from(Object raw) {
        if (raw instanceof String id) {
            return new CategoryRef(id, null);
        }
        if (raw instanceof Map<?, ?> map) {
            Object name = map.get("name");
            return new CategoryRef(String.valueOf(map.get("_id")), name == null ? null : String.valueOf(name));
        }
        throw new IllegalArgumentException("Unexpected category value: " + raw);
    }
}
