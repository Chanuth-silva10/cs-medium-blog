package io.chanuth.catalog.dtos;

import java.util.List;

/**
 * A concrete wrapper instead of a bare List: Redis stores bytes, and a fixed class tells the JSON
 * serializer exactly what to rebuild, without writing Java class names into the cache.
 */
public record ProductListResponse(List<ProductResponse> items, int total) {

    public static ProductListResponse of(List<ProductResponse> items) {
        return new ProductListResponse(items, items.size());
    }
}
