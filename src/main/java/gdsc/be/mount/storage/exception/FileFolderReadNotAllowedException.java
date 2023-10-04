package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileFolderReadNotAllowedException extends BusinessException {
    public FileFolderReadNotAllowedException() {
        super(ErrorCode.FILEFOLDER_READ_NOT_ALLOWED);
    }
}
