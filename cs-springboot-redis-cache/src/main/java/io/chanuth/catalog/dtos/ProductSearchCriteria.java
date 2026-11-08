package io.chanuth.catalog.dtos;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Locale;

/**
 * The query parameters of GET /products/search. Every one is optional.
 */
public record ProductSearchCriteria(
        @Size(max = 100) String name,
        @Size(max = 50) String category,
        @PositiveOrZero BigDecimal minPrice,
        @PositiveOrZero BigDecimal maxPrice,
        Boolean available) {

    /** "  iPhone " and "iphone" are the same search, so they are compared, and cached, in one form. */
    public String normalizedName() {
        return normalize(name);
    }

    public String normalizedCategory() {
        return normalize(category);
    }

    /**
     * One cache entry per distinct search. Every value is normalized first, so searches that mean the
     * same thing share an entry: "iPhone" and "iphone ", or a price of 1000 and 1000.00.
     * A missing filter is written as "*", so there is no way to confuse two different searches.
     */
    public String cacheKey() {
        return "name=" + orAny(normalizedName())
                + ";category=" + orAny(normalizedCategory())
                + ";minPrice=" + orAny(plain(minPrice))
                + ";maxPrice=" + orAny(plain(maxPrice))
                + ";available=" + orAny(available == null ? null : available.toString());
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String plain(BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().toPlainString();
    }

    private static String orAny(String value) {
        return value == null ? "*" : value;
    }
}
