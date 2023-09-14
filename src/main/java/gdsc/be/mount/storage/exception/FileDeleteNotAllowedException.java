package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileDeleteNotAllowedException extends BusinessException {
    public final static BusinessException EXCEPTION = new FileDeleteNotAllowedException();
    private FileDeleteNotAllowedException() {
        super(ErrorCode.FILE_DELETE_NOT_ALLOWED);
    }
}
