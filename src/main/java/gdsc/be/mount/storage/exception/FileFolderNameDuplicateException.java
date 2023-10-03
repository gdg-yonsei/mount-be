package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileFolderNameDuplicateException extends BusinessException{
    public FileFolderNameDuplicateException() {
        super(ErrorCode.FILEFOLDER_NAME_DUPLICATED);
    }
}
