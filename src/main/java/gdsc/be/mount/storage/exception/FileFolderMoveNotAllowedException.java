package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileFolderMoveNotAllowedException extends BusinessException {
    public FileFolderMoveNotAllowedException() {
        super(ErrorCode.FILEFOLDER_MOVE_NOT_ALLOWED);
    }
}
