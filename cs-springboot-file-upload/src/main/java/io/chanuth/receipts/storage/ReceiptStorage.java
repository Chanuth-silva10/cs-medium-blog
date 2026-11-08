package io.chanuth.receipts.storage;

import io.chanuth.receipts.storage.StorageExceptions.EmptyFileException;
import io.chanuth.receipts.storage.StorageExceptions.ReceiptNotFoundException;
import io.chanuth.receipts.storage.StorageExceptions.StorageException;
import io.chanuth.receipts.storage.StorageExceptions.UnsupportedFileTypeException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;

/**
 * Stores receipts on the local disk. Every receipt gets two files named after a random UUID:
 * the receipt itself ({id}.pdf, {id}.png or {id}.jpg) and its details ({id}.json).
 */
@Service
public class ReceiptStorage {

    private final Path root;
    private final JsonMapper jsonMapper;

    public ReceiptStorage(StorageProperties properties, JsonMapper jsonMapper) {
        this.root = Path.of(properties.location()).toAbsolutePath().normalize();
        this.jsonMapper = jsonMapper;
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            // Fail at startup, not on the first upload.
            throw new StorageException("Could not create the storage folder " + root, e);
        }
    }

    public Path root() {
        return root;
    }

    public Receipt store(MultipartFile file, String employeeName, LocalDate expenseDate, BigDecimal amount) {
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }
        FileType type = detectType(file);
        UUID id = UUID.randomUUID();
        Path target = root.resolve(id + "." + type.extension());
        Path temp = root.resolve(id + ".uploading");

        try (InputStream in = file.getInputStream()) {
            // Write to a temporary name first, then rename. Readers never see a half-written file.
            Files.copy(in, temp);
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            deleteQuietly(temp);
            throw new StorageException("Could not store the receipt", e);
        }

        Receipt receipt = new Receipt(id, employeeName.trim(), expenseDate, amount, type, file.getSize(),
                displayName(file.getOriginalFilename()), Instant.now());
        try {
            jsonMapper.writeValue(metadataFile(id).toFile(), receipt);
        } catch (RuntimeException e) {
            deleteQuietly(target);
            throw new StorageException("Could not store the receipt details", e);
        }
        return receipt;
    }

    public Receipt find(UUID id) {
        Path metadata = metadataFile(id);
        if (!Files.exists(metadata)) {
            throw new ReceiptNotFoundException(id);
        }
        return jsonMapper.readValue(metadata.toFile(), Receipt.class);
    }

    /** The stored file of a receipt. The path is built from a UUID, never from user text. */
    public Path file(Receipt receipt) {
        return root.resolve(receipt.id() + "." + receipt.fileType().extension());
    }

    public List<Receipt> list() {
        try (Stream<Path> files = Files.list(root)) {
            return files.filter((path) -> path.getFileName().toString().endsWith(".json"))
                    .map((path) -> jsonMapper.readValue(path.toFile(), Receipt.class))
                    .sorted(Comparator.comparing(Receipt::uploadedAt).reversed())
                    .toList();
        } catch (IOException e) {
            throw new StorageException("Could not list receipts", e);
        }
    }

    public void delete(UUID id) {
        Receipt receipt = find(id);
        deleteQuietly(file(receipt));
        deleteQuietly(metadataFile(id));
    }

    /** Reads the first bytes of the upload and decides what the file really is. */
    private static FileType detectType(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] head = in.readNBytes(FileType.SIGNATURE_LENGTH);
            return FileType.detect(head).orElseThrow(UnsupportedFileTypeException::new);
        } catch (IOException e) {
            throw new StorageException("Could not read the upload", e);
        }
    }

    /**
     * The client's file name is only a label for people. Keep the last path part, drop control
     * characters and keep it short. It is never used to build a path on the server.
     */
    static String displayName(String original) {
        if (original == null || original.isBlank()) {
            return "receipt";
        }
        String name = original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        name = name.replaceAll("\\p{Cntrl}", "").trim();
        if (name.isEmpty()) {
            return "receipt";
        }
        return name.length() > 100 ? name.substring(0, 100) : name;
    }

    private Path metadataFile(UUID id) {
        return root.resolve(id + ".json");
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best effort cleanup.
        }
    }
}
