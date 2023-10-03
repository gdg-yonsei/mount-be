package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileFolderNotFoundException extends BusinessException {
    public FileFolderNotFoundException() {
        super(ErrorCode.FILEFOLDER_NOT_FOUND);
    }
}
