package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileEmptyException extends BusinessException{
    public FileEmptyException() {
        super(ErrorCode.FILE_EMPTY);
    }
}