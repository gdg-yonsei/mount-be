package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileFolderDownloadNotAllowedException extends BusinessException {
    public FileFolderDownloadNotAllowedException() {
        super(ErrorCode.FILEFOLDER_DOWNLOAD_NOT_ALLOWED);
    }
}
