package io.chanuth.receipts.web;

import io.chanuth.receipts.storage.Receipt;
import io.chanuth.receipts.storage.ReceiptStorage;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {

    private final ReceiptStorage storage;

    public ReceiptController(ReceiptStorage storage) {
        this.storage = storage;
    }

    /**
     * A multipart/form-data request: one part named "file" holds the receipt, the other parts hold
     * the text fields. The file is required; a request without it gets a clear 400, not a crash.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptResponse> upload(@RequestPart("file") MultipartFile file,
                                                  @Valid @ModelAttribute ReceiptForm form) {
        Receipt receipt = storage.store(file, form.employeeName(), form.expenseDate(), form.amount());
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(receipt.id()).toUri();
        return ResponseEntity.created(location).body(ReceiptResponse.from(receipt));
    }

    @GetMapping
    public List<ReceiptResponse> list() {
        return storage.list().stream().map(ReceiptResponse::from).toList();
    }

    /** The id is a UUID: anything else is refused with 400 before we touch the disk. */
    @GetMapping("/{id}")
    public ReceiptResponse details(@PathVariable UUID id) {
        return ReceiptResponse.from(storage.find(id));
    }

    /**
     * Serves the stored file. The Content-Type comes from what we detected at upload time, never
     * from the client, and nosniff stops the browser from guessing a more dangerous type.
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> file(@PathVariable UUID id,
                                         @RequestParam(defaultValue = "false") boolean download) {
        Receipt receipt = storage.find(id);
        String name = "receipt-" + receipt.id() + "." + receipt.fileType().extension();
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(name).build();

        return ResponseEntity.ok()
                .contentType(receipt.fileType().mediaType())
                .contentLength(receipt.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore())
                .body(new FileSystemResource(storage.file(receipt)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        storage.delete(id);
        return ResponseEntity.noContent().build();
    }
}
