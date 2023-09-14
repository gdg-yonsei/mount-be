package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileUploadException extends BusinessException {
    public final static BusinessException EXCEPTION = new FileUploadException();

    private FileUploadException() {
        super(ErrorCode.FILE_UPLOAD_FAILED);
    }

}