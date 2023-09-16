package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FolderUpdateException extends BusinessException {
    public final static BusinessException EXCEPTION = new FolderUpdateException();

    private FolderUpdateException() {
        super(ErrorCode.FOLDER_UPDATE_FAILED);
    }
}
