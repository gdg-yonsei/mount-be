package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FolderNameDuplicateException extends BusinessException{
    public final static BusinessException EXCEPTION = new FolderNameDuplicateException();

    private FolderNameDuplicateException() {
        super(ErrorCode.FOLDER_NAME_DUPLICATED);
    }
}
