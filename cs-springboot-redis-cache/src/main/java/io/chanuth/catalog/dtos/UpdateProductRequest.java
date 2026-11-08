package io.chanuth.catalog.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

/** A partial update: only the price and the availability change often enough to matter here. */
public record UpdateProductRequest(
        @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price,
        Boolean available) {
}
