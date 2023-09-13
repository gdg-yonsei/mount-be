package gdsc.be.mount.storage.exception;

public class FileStorageExeption extends RuntimeException {
    public FileStorageExeption(String message) {
        super(message);
    }

    public FileStorageExeption(String message, Throwable cause) {
        super(message, cause);
    }

}
