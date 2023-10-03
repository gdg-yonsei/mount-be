package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileFolderDeleteNotAllowedException extends BusinessException {
    public FileFolderDeleteNotAllowedException() {
        super(ErrorCode.FILEFOLDER_DELETE_NOT_ALLOWED);
    }
}
