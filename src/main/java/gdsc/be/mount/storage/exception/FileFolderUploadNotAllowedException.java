package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileFolderUploadNotAllowedException extends BusinessException {
    public FileFolderUploadNotAllowedException() {
        super(ErrorCode.FILEFOLDER_UPLOAD_NOT_ALLOWED);
    }
}
