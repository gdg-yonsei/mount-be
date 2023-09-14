package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileDownloadNotAllowedException extends BusinessException {

    public final static BusinessException EXCEPTION = new FileDownloadNotAllowedException();
    private FileDownloadNotAllowedException() {
        super(ErrorCode.FILE_DOWNLOAD_NOT_ALLOWED);
    }
}
