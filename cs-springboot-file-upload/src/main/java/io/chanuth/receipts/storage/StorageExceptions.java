package io.chanuth.receipts.storage;

/** The ways storing or reading a receipt can fail, each mapped to its own HTTP status. */
public final class StorageExceptions {

    private StorageExceptions() {
    }

    /** 415: the bytes are not a PDF, PNG or JPEG, whatever the file name says. */
    public static class UnsupportedFileTypeException extends RuntimeException {
        public UnsupportedFileTypeException() {
            super("Only PDF, PNG and JPEG files are accepted");
        }
    }

    /** 422: the upload had a file part, but it was empty. */
    public static class EmptyFileException extends RuntimeException {
        public EmptyFileException() {
            super("The file is empty");
        }
    }

    /** 404 */
    public static class ReceiptNotFoundException extends RuntimeException {
        public ReceiptNotFoundException(Object id) {
            super("No receipt with id " + id);
        }
    }

    /** 500: the disk itself failed. */
    public static class StorageException extends RuntimeException {
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
