package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileUpdateNotAllowedException extends BusinessException {
    public final static BusinessException EXCEPTION = new FileUpdateNotAllowedException();
    private FileUpdateNotAllowedException() {
        super(ErrorCode.FILE_UPDATE_NOT_ALLOWED);
    }
}
