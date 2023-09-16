package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FolderCreateException extends BusinessException{
    public final static BusinessException EXCEPTION = new FolderCreateException();

    private FolderCreateException() {
        super(ErrorCode.FOLDER_CREATE_FAILED);
    }
}
