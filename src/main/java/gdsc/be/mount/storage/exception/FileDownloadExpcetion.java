package gdsc.be.mount.storage.exception;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.exception.BusinessException;

public class FileDownloadExpcetion extends BusinessException {
    public final static BusinessException EXCEPTION = new FileDownloadExpcetion();
    private FileDownloadExpcetion() {
        super(ErrorCode.FILE_DOWNLOAD_FAILED);
    }
}
