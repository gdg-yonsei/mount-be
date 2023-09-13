package gdsc.be.mount.storage.exception;

public class FileDownloadNotAllowedException extends RuntimeException{
    public FileDownloadNotAllowedException(String message) {
        super(message);
    }

    public FileDownloadNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
