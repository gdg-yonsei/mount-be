package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileFolderDeletionException extends BusinessException {
    public FileFolderDeletionException() {
        super(ErrorCode.FILEFOLDER_DELETE_FAILED);
    }
}
