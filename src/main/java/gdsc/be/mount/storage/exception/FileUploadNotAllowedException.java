package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileUploadNotAllowedException extends BusinessException {

    public final static BusinessException EXCEPTION = new FileUploadNotAllowedException();
    private FileUploadNotAllowedException() {
        super(ErrorCode.FILE_UPLOAD_NOT_ALLOWED);
    }
}
