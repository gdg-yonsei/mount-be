package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileFolderUploadException extends BusinessException {
    public FileFolderUploadException() {
        super(ErrorCode.FILEFOLDER_UPLOAD_FAILED);
    }
}