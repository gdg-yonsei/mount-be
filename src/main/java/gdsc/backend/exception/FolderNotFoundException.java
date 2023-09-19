package gdsc.backend.exception;

public class FolderNotFoundException extends RuntimeException{

        public FolderNotFoundException(String message) {
            super(message);
        }

        public FolderNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
}
