package io.chanuth.receipts.web;

import io.chanuth.receipts.storage.Receipt;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public record ReceiptResponse(
        UUID id,
        String employeeName,
        LocalDate expenseDate,
        BigDecimal amount,
        String contentType,
        long sizeBytes,
        String originalFilename,
        Instant uploadedAt,
        String fileUrl) {

    public static ReceiptResponse from(Receipt receipt) {
        // Built from the incoming request, so the link is right behind any host name or port.
        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/receipts/{id}/file").buildAndExpand(receipt.id()).toUriString();
        return new ReceiptResponse(receipt.id(), receipt.employeeName(), receipt.expenseDate(), receipt.amount(),
                receipt.fileType().mediaType().toString(), receipt.sizeBytes(), receipt.originalFilename(),
                receipt.uploadedAt(), fileUrl);
    }
}
