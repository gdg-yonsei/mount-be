package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileFolderUpdateNotAllowedException extends BusinessException {
    public FileFolderUpdateNotAllowedException() {
        super(ErrorCode.FILEFOLDER_UPDATE_NOT_ALLOWED);
    }
}
