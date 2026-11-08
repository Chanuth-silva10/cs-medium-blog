package io.chanuth.receipts.storage;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Everything we know about one uploaded receipt. It is saved as a small JSON file next to the
 * receipt itself, so the information survives a restart without needing a database.
 *
 * @param originalFilename the name the client sent, shown to people only; never used on disk
 */
public record Receipt(
        UUID id,
        String employeeName,
        LocalDate expenseDate,
        BigDecimal amount,
        FileType fileType,
        long sizeBytes,
        String originalFilename,
        Instant uploadedAt) {
}
