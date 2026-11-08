package io.chanuth.receipts.storage;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.MediaType;

/**
 * The only file types we accept. Each one is recognised by its first bytes (its "magic number"),
 * because the file name and the Content-Type are both chosen by the client and prove nothing.
 */
public enum FileType {

    PDF("pdf", MediaType.APPLICATION_PDF, new byte[] {'%', 'P', 'D', 'F', '-'}),
    PNG("png", MediaType.IMAGE_PNG, new byte[] {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'}),
    JPEG("jpg", MediaType.IMAGE_JPEG, new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});

    /** The longest signature, so we know how many bytes to read. */
    public static final int SIGNATURE_LENGTH = 8;

    private final String extension;
    private final MediaType mediaType;
    private final byte[] signature;

    FileType(String extension, MediaType mediaType, byte[] signature) {
        this.extension = extension;
        this.mediaType = mediaType;
        this.signature = signature;
    }

    public static Optional<FileType> detect(byte[] firstBytes) {
        return Arrays.stream(values()).filter((type) -> type.matches(firstBytes)).findFirst();
    }

    public static Optional<FileType> fromExtension(String extension) {
        return Arrays.stream(values()).filter((type) -> type.extension.equals(extension)).findFirst();
    }

    private boolean matches(byte[] firstBytes) {
        if (firstBytes.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (firstBytes[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    public String extension() {
        return extension;
    }

    public MediaType mediaType() {
        return mediaType;
    }
}
