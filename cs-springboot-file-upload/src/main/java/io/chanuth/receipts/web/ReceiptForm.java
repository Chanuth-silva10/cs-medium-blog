package io.chanuth.receipts.web;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The text fields sent next to the file in the same multipart form. Spring binds them by name,
 * and turns "2026-11-03" into a LocalDate for us, so a date that is not a date is refused.
 */
public record ReceiptForm(
        @NotBlank @Size(max = 100) String employeeName,
        @NotNull @PastOrPresent LocalDate expenseDate,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal amount) {
}
