package io.chanuth.catalog.dtos;

import io.chanuth.catalog.entities.Product;
import java.math.BigDecimal;

/**
 * What we cache and return. Caching the JPA entity instead would be a trap: it holds a lazy
 * Hibernate proxy for the category, which cannot be turned into JSON outside a database session.
 */
public record ProductResponse(Long id, String name, String description, BigDecimal price, boolean available,
                              String category) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(),
                product.getPrice(), product.isAvailable(), product.getCategory().getName());
    }
}
